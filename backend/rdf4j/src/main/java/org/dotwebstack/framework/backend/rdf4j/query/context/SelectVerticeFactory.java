package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.deepList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;

import graphql.schema.GraphQLFieldDefinition;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.springframework.stereotype.Component;

@Component
public class SelectVerticeFactory extends AbstractVerticeFactory {

  public SelectVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(Variable subject, NodeShape nodeShape, List<FilterRule> filterRules, List<OrderBy> orderBys,
      OuterQuery<?> query) {
    Vertice vertice = createVertice(subject, query, nodeShape, filterRules);
    orderBys.forEach(orderBy -> addOrderables(vertice, query, orderBy, nodeShape));
    return vertice;
  }

  private Vertice createVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<FilterRule> filterRules) {
    Vertice vertice = Vertice.builder()
        .subject(subject)
        .nodeShape(nodeShape)
        .build();

    addRequiredEdges(vertice, nodeShape.getPropertyShapes()
        .values(), query);

    filterRules.forEach(filter -> applyFilter(vertice, nodeShape, filter, query));

    addConstraints(vertice, query);
    return vertice;
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
          edge = createSimpleEdge(edgeSubject, nodeShape.getPropertyShape(fieldDefinition.getName()), false, false);

          if (Objects.nonNull(fieldDefinition.getDirective(Rdf4jDirectives.AGGREGATE_NAME))) {
            edge.setOptional(true);
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
            .ifPresent(edge1 -> {
              vertice.getEdges()
                  .add(edge1);
              deepList(singletonList(edge1)).forEach(e -> e.setOptional(false));
            });
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
