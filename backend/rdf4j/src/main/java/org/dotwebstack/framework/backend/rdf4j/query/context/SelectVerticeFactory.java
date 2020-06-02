package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.deepList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;

import graphql.schema.GraphQLFieldDefinition;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.springframework.stereotype.Component;

@Component
public class SelectVerticeFactory extends AbstractVerticeFactory {

  public SelectVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  /*
   * Check which edges should be added to the where part of the query based on a sh:minCount property
   * of 1
   */
  static void addConstraints(@NonNull Vertice vertice, @NonNull NodeShape nodeShape, OuterQuery<?> query) {
    nodeShape.getPropertyShapes()
        .values()
        .stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1)
        .forEach(ps -> {
          if (ps.getHasValue() != null) {
            Constraint constraint = Constraint.builder()
                .predicate(ps.getPath()
                    .toPredicate())
                .values(Set.of((Resource) ps.getHasValue()))
                .constraintType(ConstraintType.HASVALUE)
                .build();
            vertice.getConstraints()
                .add(constraint);
          } else {
            Variable var = query.var();
            Edge edge = createSimpleEdge(var, ps, true, false);
            vertice.getEdges()
                .add(edge);
            if (ps.getNode() != null) {
              Vertice childVertice = edge.getObject();
              addConstraints(childVertice, ps.getNode(), query);
            }
          }
        });
  }

  public Vertice createRoot(Variable subject, NodeShape nodeShape, List<FilterRule> filterRules, List<OrderBy> orderBys,
      OuterQuery<?> query) {
    Vertice vertice = createVertice(subject, query, nodeShape, filterRules);
    orderBys.forEach(orderBy -> addOrderables(vertice, query, orderBy, nodeShape));
    return vertice;
  }

  private Vertice createVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<FilterRule> filterRules) {
    Vertice vertice = createVertice(subject, nodeShape, query);

    filterRules.forEach(filter -> applyFilter(vertice, nodeShape, filter, query));
    return vertice;
  }

  private Vertice createVertice(final Variable subject, @NonNull NodeShape nodeShape, @NonNull OuterQuery<?> query) {
    Vertice vertice = Vertice.builder()
        .subject(subject)
        .build();

    Set<Value> iris = nodeShape.getClasses()
        .stream()
        .map(targetClass -> (Value) targetClass)
        .collect(Collectors.toSet());

    if (!iris.isEmpty()) {
      vertice.getConstraints()
          .addAll(Set.of(Constraint.builder()
              .constraintType(ConstraintType.RDF_TYPE)
              .predicate(() -> stringify(RDF.TYPE))
              .values(iris)
              .build()));
    }

    addConstraints(vertice, nodeShape, query);
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
