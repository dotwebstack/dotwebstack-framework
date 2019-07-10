package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.joinExpressions;

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
    return vertice.getEdges()
        .stream()
        .flatMap(edge -> getConstructPatterns(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
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

    patterns.addAll(vertice.getEdges()
        .stream()
        .flatMap(edge -> getWherePatterns(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));

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

    if (!edge.getObject()
        .getFilters()
        .isEmpty()) {
      Expression<?> expression = joinExpressions(FilterJoinType.AND, null, edge.getObject()
          .getFilters()
          .stream()
          .map(filter -> filter.toExpression(edge.getObject()
              .getSubject()))
          .collect(Collectors.toList()));
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = getWherePatterns(edge.getObject());
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    return Collections.singletonList(graphPattern);
  }
}
