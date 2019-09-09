package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.joinExpressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

@Slf4j
public class VerticeHelper {

  private VerticeHelper() {}

  public static List<TriplePattern> getConstructPatterns(Vertice vertice) {
    List<Edge> edges = vertice.getEdges();
    Collections.sort(edges);
    return edges.stream()
        .flatMap(edge -> getConstructPatterns(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static List<TriplePattern> getConstructPatterns(Edge edge, Variable subject) {
    List<TriplePattern> triplePatterns = new ArrayList<>();

    if (edge.isVisible()) {
      if (Objects.nonNull(edge.getObject()
          .getSubject())) {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), edge.getObject()
            .getSubject()));
      }
    }

    triplePatterns.addAll(getConstructPatterns(edge.getObject()));

    return triplePatterns;
  }

  public static List<GraphPattern> getWherePatterns(Vertice vertice) {

    List<Edge> edges = vertice.getEdges();
    Collections.sort(edges);

    return edges.stream()
        .flatMap(edge -> getWherePatterns(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static List<GraphPattern> getWherePatterns(Edge edge, Variable subject) {
    GraphPattern graphPattern = (Objects.nonNull(edge.getObject()
        .getSubject())) ? GraphPatterns.tp(subject, edge.getPredicate(),
            edge.getObject()
                .getSubject())
            .optional(edge.isOptional()) : getTriplePatternForIris(edge, subject);

    if (!edge.getObject()
        .getFilters()
        .isEmpty()) {
      Expression<?> expression = joinExpressions(FilterJoinType.AND, null, edge.getObject()
          .getFilters()
          .stream()
          .map(filter -> getFilterExpression(filter, Objects.nonNull(edge.getObject()
              .getSubject()) ? edge.getObject()
                  .getSubject() : subject))
          .collect(Collectors.toList()));
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = getWherePatterns(edge.getObject());
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    return singletonList(graphPattern);
  }

  private static GraphPattern getTriplePatternForIris(Edge edge, Variable subject) {
    if (edge.getObject()
        .getIris()
        .size() == 1) {
      return GraphPatterns.tp(subject, edge.getPredicate(), edge.getObject()
          .getIris()
          .iterator()
          .next())
          .optional(edge.isOptional());
    }

    return GraphPatterns.union(edge.getObject()
        .getIris()
        .stream()
        .map(iri -> GraphPatterns.tp(subject, edge.getPredicate(), iri)
            .optional(edge.isOptional()))
        .toArray(GraphPattern[]::new));
  }

  private static Expression<?> getFilterExpression(Filter filter, Variable subject) {
    List<Expression<?>> expressions = filter.getOperands()
        .stream()
        .map(operand -> FilterHelper.getExpressionFromOperator(subject, filter.getOperator(), operand))
        .collect(Collectors.toList());
    return FilterHelper.joinExpressions(FilterJoinType.OR, null, expressions);
  }
}
