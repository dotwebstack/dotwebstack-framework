package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

public class VerticeHelper {

  private VerticeHelper() {}

  public static List<TriplePattern> getConstructPatterns(Vertice vertice) {
    if (Objects.nonNull(vertice.getEdges()) && !vertice.getEdges()
        .isEmpty()) {
      return vertice.getEdges()
          .stream()
          .flatMap(edge -> getConstructPatterns(edge, vertice.getSubject()).stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private static List<TriplePattern> getConstructPatterns(Edge edge, Variable subject) {
    List<TriplePattern> triplePatterns = new ArrayList<>();

    if (edge.isVisible()) {
      if (Objects.nonNull(edge.getObject()
          .getSubject())) {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getPredicate(), edge.getObject()
            .getSubject()));
      } else {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getPredicate(), edge.getObject()
            .getIri()));
      }
    }

    triplePatterns.addAll(getConstructPatterns(edge.getObject()));

    return triplePatterns;
  }

  public static List<GraphPattern> getWherePatterns(Vertice vertice) {
    List<GraphPattern> patterns = new ArrayList<>();
    if (Objects.nonNull(vertice.getEdges()) && !vertice.getEdges()
        .isEmpty()) {
      patterns.addAll(vertice.getEdges()
          .stream()
          .flatMap(edge -> getWherePatterns(edge, vertice.getSubject()).stream())
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));
    }

    return patterns;
  }

  private static List<GraphPattern> getWherePatterns(Edge edge, Variable subject) {
    GraphPattern graphPattern = (Objects.nonNull(edge.getObject()
        .getSubject())) ? GraphPatterns.tp(subject, edge.getPredicate(),
            edge.getObject()
                .getSubject())
            .optional(edge.isOptional())
            : GraphPatterns.tp(subject, edge.getPredicate(), edge.getObject()
                .getIri())
                .optional(edge.isOptional());

    List<Filter> filters = edge.getObject()
        .getFilters();
    if (Objects.nonNull(filters) && !filters.isEmpty()) {
      List<Expression<?>> expressions = filters.stream()
          .map(filter -> filter.getExpression(edge.getObject()
              .getSubject()))
          .collect(Collectors.toList());
      Expression<?> expression = FilterHelper.joinExpressions(FilterJoinType.AND, null, expressions);
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = getWherePatterns(edge.getObject());
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    return Collections.singletonList(graphPattern);
  }
}
