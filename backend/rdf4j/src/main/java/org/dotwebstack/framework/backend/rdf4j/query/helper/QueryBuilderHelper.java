package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FilterHelper.joinExpressions;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.query.model.AggregateType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Filter;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Aggregate;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Assignment;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.QueryElement;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfObject;

@Slf4j
public class QueryBuilderHelper {

  private static final String OPTIONAL = "OPTIONAL";

  private QueryBuilderHelper() {}

  public static List<TriplePattern> buildConstructTriples(@NonNull Vertice vertice) {
    List<Edge> edges = vertice.getEdges();
    List<TriplePattern> triplePatterns = edges.stream()
        .flatMap(edge -> buildConstructTriples(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    triplePatterns.addAll(buildTypeConstraintTriples(vertice));
    sortQueryElements(triplePatterns);
    return triplePatterns;
  }

  private static List<TriplePattern> buildConstructTriples(Edge edge, Variable subject) {
    List<TriplePattern> triplePatterns = new ArrayList<>();

    if (edge.isVisible()) {
      if (Objects.nonNull(edge.getAggregate())) {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), edge.getAggregate()
            .getVariable()));
      }
      triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), edge.getObject()
          .getSubject()));
    }

    triplePatterns.addAll(buildConstructTriples(edge.getObject()));

    return triplePatterns;
  }

  private static List<TriplePattern> buildTypeConstraintTriples(Vertice vertice) {
    return vertice.getConstraints()
        .stream()
        .filter(constraint -> stringify(RDF.TYPE).equals(constraint.getPredicate()
            .getQueryString()))
        .flatMap(typeConstraint -> typeConstraint.getValues()
            .stream()
            .map(value -> {
              if (value instanceof Value) {
                return GraphPatterns.tp(vertice.getSubject(), typeConstraint.getPredicate(), (Value) value);
              } else {
                return GraphPatterns.tp(vertice.getSubject(), typeConstraint.getPredicate(), (RdfObject) value);
              }
            }))
        .collect(Collectors.toList());
  }

  private static List<GraphPattern> buildFilterTriples(List<GraphPattern> result, List<Filter> filtersWithVariables) {
    GraphPatternNotTriples graphPatternNotTriples = GraphPatterns.and(result.toArray(GraphPattern[]::new));

    filtersWithVariables
        .forEach(filter -> graphPatternNotTriples
            .filter(Expressions.equals(Expressions.coalesce(filter.getVariable(), Rdf.literalOf(0)),
                filter.getOperands()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> illegalArgumentException("No operand found for filter!")))));

    return singletonList(graphPatternNotTriples);
  }

  private static List<GraphPattern> buildConstraintTriples(Vertice vertice) {
    return vertice.getConstraints()
        .stream()
        .flatMap(constraint -> Stream.of(buildTripleForConstraint(vertice.getSubject(), constraint)))
        .collect(Collectors.toList());
  }

  public static List<GraphPattern> buildWhereTriples(@NonNull Vertice vertice) {
    List<Edge> edges = vertice.getEdges();

    List<GraphPattern> graphPatterns = edges.stream()
        .flatMap(edge -> buildWhereTriples(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    graphPatterns.addAll(buildConstraintTriples(vertice));

    List<Filter> filtersWithEdge = getNonNullFilters(vertice);
    sortQueryElements(graphPatterns);
    if (!filtersWithEdge.isEmpty()) {
      return buildFilterTriples(graphPatterns, filtersWithEdge);
    }
    return graphPatterns;
  }

  private static List<GraphPattern> buildWhereTriples(Edge edge, Variable subject) {
    GraphPattern graphPattern = GraphPatterns.tp(subject, edge.getPredicate(), edge.getObject()
        .getSubject())
        .optional(edge.isOptional());

    if (!edge.getObject()
        .getFilters()
        .isEmpty()) {
      Expression<?> expression = joinExpressions(FilterJoinType.AND, null, edge.getObject()
          .getFilters()
          .stream()
          .map(filter -> buildFilterExpression(filter, getSubject(edge, subject)))
          .collect(Collectors.toList()));
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = buildWhereTriples(edge.getObject());
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    if (Objects.nonNull(edge.getAggregate()) && Objects.equals(AggregateType.COUNT, edge.getAggregate()
        .getType())) {
      graphPattern = GraphPatterns.select(getSelectedForCount(subject, subject, edge).toArray(new Projectable[] {}))
          .where(graphPattern)
          .groupBy(subject)
          .optional(true);
    }

    return singletonList(graphPattern);
  }

  private static void sortQueryElements(List<? extends QueryElement> queryElements) {
    queryElements.sort((one, other) -> {
      if (one.getQueryString()
          .startsWith(OPTIONAL)
          && other.getQueryString()
              .startsWith(OPTIONAL)) {
        return 0;
      }
      return one.getQueryString()
          .startsWith(OPTIONAL) ? 1 : -1;
    });
  }

  private static Set<Projectable> getSelectedForCount(Variable root, Variable subject, Edge edge) {
    Set<Projectable> result = new LinkedHashSet<>();
    if (!Objects.equals(root, subject)) {
      result.add(subject);
    }
    result.add(root);

    Aggregate aggregate = Expressions.count(edge.getObject()
        .getSubject());

    Assignment assignable = SparqlBuilder.as(aggregate, edge.getAggregate()
        .getVariable());

    result.add(assignable);

    return result;
  }

  private static List<Filter> getNonNullFilters(@NonNull Vertice vertice) {
    return vertice.getFilters()
        .stream()
        .filter(filter -> Objects.nonNull(filter.getVariable()))
        .collect(Collectors.toList());
  }

  static Variable getSubject(Edge edge, Variable subject) {
    if (Objects.nonNull(edge.getAggregate())) {
      return edge.getAggregate()
          .getVariable();
    }

    if (Objects.nonNull(edge.getObject()
        .getSubject())) {
      return edge.getObject()
          .getSubject();
    }

    return subject;
  }

  private static GraphPattern buildTripleForConstraint(Variable subject, Constraint constraint) {
    if (constraint.getValues()
        .size() == 1) {
      Object value = constraint.getValues()
          .iterator()
          .next();
      if (value instanceof Value) {
        return GraphPatterns.tp(subject, constraint.getPredicate(), (Value) value)
            .optional(constraint.isOptional());
      } else {
        return GraphPatterns.tp(subject, constraint.getPredicate(), (RdfObject) value)
            .optional(constraint.isOptional());
      }
    }

    return GraphPatterns.union(constraint.getValues()
        .stream()
        .map(value -> {
          if (value instanceof Value) {
            return GraphPatterns.tp(subject, constraint.getPredicate(), (Value) value)
                .optional(constraint.isOptional());
          } else {
            return GraphPatterns.tp(subject, constraint.getPredicate(), (RdfObject) value)
                .optional(constraint.isOptional());
          }
        })
        .toArray(GraphPattern[]::new));
  }

  private static Expression<?> buildFilterExpression(Filter filter, Variable subject) {
    List<Expression<?>> expressions = filter.getOperands()
        .stream()
        .map(operand -> FilterHelper.buildExpressionFromOperator(subject, filter.getOperator(), operand))
        .collect(Collectors.toList());
    return FilterHelper.joinExpressions(FilterJoinType.OR, null, expressions);
  }
}
