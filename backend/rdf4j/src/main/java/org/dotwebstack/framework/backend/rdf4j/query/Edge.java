package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionContext;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionHelper;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Getter
@Setter
@Builder
class Edge {

  private RdfPredicate predicate;

  private Vertice object;

  private boolean isOptional;

  private boolean isVisible;

  List<TriplePattern> getConstructPatterns(Variable subject) {
    List<TriplePattern> triplePatterns = new ArrayList<>();

    if (isVisible) {
      if (!Objects.isNull(object.getSubject())) {
        triplePatterns.add(GraphPatterns.tp(subject, predicate, object.getSubject()));
      } else {
        triplePatterns.add(GraphPatterns.tp(subject, predicate, object.getIri()));
      }
    }

    triplePatterns.addAll(object.getConstructPatterns());

    return triplePatterns;
  }

  List<GraphPattern> getWherePatterns(Variable subject) {
    GraphPattern graphPattern = (!Objects.isNull(object.getSubject()))
        ? GraphPatterns.tp(subject, predicate, object.getSubject())
            .optional(isOptional)
        : GraphPatterns.tp(subject, predicate, object.getIri())
            .optional(isOptional);

    List<ExpressionContext> expressionContexts = object.getFilters();
    if (!Objects.isNull(expressionContexts) && !expressionContexts.isEmpty()) {
      List<Expression<?>> expressions = expressionContexts.stream()
          .map(filter -> filter.getExpression(object.getSubject()))
          .collect(Collectors.toList());
      Expression<?> expression = ExpressionHelper.joinExpressions(FilterJoinType.AND, null, expressions);
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = object.getWherePatterns();
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    return Collections.singletonList(graphPattern);
  }
}
