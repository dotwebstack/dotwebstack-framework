package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.springframework.stereotype.Component;

@Component
public class SelectVerticeFactory extends AbstractVerticeFactory {

  public SelectVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(Variable subject, OuterQuery<?> query, NodeShape nodeShape, List<FilterRule> filterRules,
      List<Object> orderByList) {
    Vertice vertice = createVertice(subject, query, nodeShape, filterRules);
    makeEdgesUnique(vertice.getEdges());
    orderByList.forEach(orderBy -> addOrderables(vertice, query, castToMap(orderBy), nodeShape));
    return vertice;
  }

  private Vertice createVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<FilterRule> filterRules) {
    Vertice vertice = createVertice(subject, nodeShape);

    processVertice(vertice, nodeShape, filterRules, query);

    return vertice;
  }

  private void processVertice(Vertice vertice, NodeShape nodeShape, List<FilterRule> filterRules, OuterQuery<?> query) {
    processResourceVertice(vertice, nodeShape, filterRules, query);
    processChildVertice(vertice, nodeShape, filterRules, query);
    processNestedVertice(vertice, nodeShape, filterRules, query);
  }

  private void processResourceVertice(Vertice vertice, NodeShape nodeShape, List<FilterRule> filterRules,
      OuterQuery<?> query) {
    filterRules.stream()
        .filter(FilterRule::isResource)
        .forEach(filterRule -> addFilterToVertice(vertice, query, nodeShape, filterRule));
  }

  private void processChildVertice(Vertice vertice, NodeShape nodeShape, List<FilterRule> filterRules,
      OuterQuery<?> query) {
    filterRules.stream()
        .filter(filterRule -> !filterRule.isResource())
        .filter(filterRule -> nodeShape.equals(getNextNodeShape(nodeShape, filterRule.getPath())))
        .forEach(filterRule -> addFilterToVertice(vertice, query, nodeShape, filterRule));
  }

  private void processNestedVertice(Vertice vertice, NodeShape nodeShape, List<FilterRule> filterRules,
      OuterQuery<?> query) {
    filterRules.stream()
        .filter(filterRule -> !filterRule.isResource())
        .filter(filterRule -> !nodeShape.equals(getNextNodeShape(nodeShape, filterRule.getPath())))
        .forEach(filterRule -> processVertice(vertice, query, nodeShape, filterRule));
  }

  private void processVertice(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    vertice.getEdges()
        .add(getEdge(query, nodeShape, filterRule));
  }

  private Edge getEdge(OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule) {
    boolean isNested = filterRule.getPath()
        .size() != 1;

    NodeShape childShape = getNextNodeShape(nodeShape, filterRule.getPath());
    RdfPredicate predicate = getPredicate(nodeShape, filterRule);

    return isNested ? processNestedEdge(query, filterRule, childShape, predicate)
        : processEdge(query, filterRule, childShape, predicate);
  }

  private Edge processNestedEdge(OuterQuery<?> query, FilterRule filterRule, NodeShape childShape,
      RdfPredicate predicate) {
    FilterRule childFilterRule = getFilterRule(filterRule);

    Vertice childVertice = createVertice(query.var(), query, childShape, Collections.singletonList(childFilterRule));

    return buildEdge(predicate, null, childVertice, false, false);
  }

  private Edge processEdge(OuterQuery<?> query, FilterRule filterRule, NodeShape childShape, RdfPredicate predicate) {
    Edge edge = createSimpleEdge(query.var(), null, predicate, false);

    addFilterToVertice(edge.getObject(), query, childShape, filterRule);
    return edge;
  }

  private RdfPredicate getPredicate(NodeShape nodeShape, FilterRule filterRule) {
    return nodeShape.getPropertyShape(filterRule.getPath()
        .get(0))
        .getPath()
        .toPredicate();
  }

  private FilterRule getFilterRule(FilterRule filter) {
    List<String> path = filter.getPath()
        .subList(1, filter.getPath()
            .size());
    return FilterRule.builder()
        .path(path)
        .value(filter.getValue())
        .operator(filter.getOperator())
        .build();
  }

  private Vertice createVertice(final Variable subject, @NonNull NodeShape nodeShape) {
    List<Edge> edges = new ArrayList<>();

    Set<Iri> iris = nodeShape.getTargetClasses()
        .stream()
        .map(targetClass -> Rdf.iri(targetClass.stringValue()))
        .collect(Collectors.toSet());

    edges.add(createSimpleEdge(null, iris, () -> stringify(RDF.TYPE), true));

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }
}
