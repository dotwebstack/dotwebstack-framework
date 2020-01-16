package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.makeEdgesUnique;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;

import graphql.schema.GraphQLFieldDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.springframework.stereotype.Component;

@Component
public class SelectVerticeFactory extends AbstractVerticeFactory {

  public SelectVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(Variable subject, NodeShape nodeShape, List<FilterRule> filterRules, List<OrderBy> orderBys,
      OuterQuery<?> query) {
    Vertice vertice = createVertice(subject, query, nodeShape, filterRules);
    makeEdgesUnique(vertice.getEdges());
    orderBys.forEach(orderBy -> addOrderables(vertice, query, orderBy, nodeShape));
    return vertice;
  }

  private Vertice createVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<FilterRule> filterRules) {
    Vertice vertice = createVertice(subject, nodeShape);

    filterRules.forEach(filter -> applyFilter(vertice, nodeShape, filter, query));
    return vertice;
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

  private void applyFilter(Vertice vertice, NodeShape nodeShape, FilterRule filterRule, OuterQuery<?> query) {
    {
      if (filterRule.getFieldPath()
          .isResource()) {
        addFilterToVertice(nodeShape, vertice, filterRule, vertice.getSubject());
        return;
      }

      NodeShape childShape = getNextNodeShape(nodeShape, filterRule.getFieldPath()
          .getFieldDefinitions());

      if (nodeShape.equals(childShape)) {
        addFilterToVertice(vertice, query, nodeShape, filterRule);
      } else {
        Variable edgeSubject = query.var();
        Edge edge;

        if (filterRule.getFieldPath()
            .rest()
            .isEmpty()) {
          GraphQLFieldDefinition fieldDefinition = filterRule.getFieldPath()
              .first();
          edge = createSimpleEdge(edgeSubject, null, nodeShape.getPropertyShape(fieldDefinition.getName())
              .getPath()
              .toPredicate(), false);

          if (Objects.nonNull(fieldDefinition.getDirective(Rdf4jDirectives.AGGREGATE_NAME))) {
            createAggregate(fieldDefinition, query.var()).ifPresent(edge::setAggregate);
            addFilterToVertice(nodeShape, vertice, filterRule, edge.getAggregate()
                .getVariable());
          } else {
            addFilterToVertice(edge.getObject(), query, childShape, filterRule);
          }

          vertice.getEdges()
              .add(edge);
        }

        filterRule.getFieldPath()
            .rest()
            .map(rest -> FilterRule.builder()
                .fieldPath(rest)
                .value(filterRule.getValue())
                .operator(filterRule.getOperator())
                .build())
            .map(childFilterRule -> createVertice(edgeSubject, query, childShape,
                Collections.singletonList(childFilterRule)))
            .map(childVertice -> createEdge(nodeShape, filterRule, childVertice))
            .ifPresent(edge1 -> vertice.getEdges()
                .add(edge1));
      }
    }
  }

  private Edge createEdge(NodeShape nodeShape, FilterRule filter, Vertice childVertice) {
    return Edge.builder()
        .predicate(nodeShape.getPropertyShape(filter.getFieldPath()
            .first()
            .getName())
            .getPath()
            .toPredicate())
        .object(childVertice)
        .isVisible(false)
        .isOptional(false)
        .build();
  }
}
