package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.FilterHelper.joinExpressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Aggregate;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Assignment;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

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
      if (Objects.nonNull(edge.getAggregate())) {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), edge.getAggregate()
            .getVariable()));
      } else if (Objects.nonNull(edge.getObject()
          .getSubject())) {
        triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), edge.getObject()
            .getSubject()));
      } else {
        edge.getObject()
            .getIris()
            .forEach(iri -> {
              triplePatterns.add(GraphPatterns.tp(subject, edge.getConstructPredicate(), iri));
            });
      }
    }

    triplePatterns.addAll(getConstructPatterns(edge.getObject()));

    return triplePatterns;
  }

  public static List<GraphPattern> getWherePatterns(@NonNull Vertice vertice) {
    List<Edge> edges = vertice.getEdges();
    Collections.sort(edges);

    List<GraphPattern> result = new ArrayList<>();

    result.addAll(edges.stream()
        .flatMap(edge -> getWherePatterns(edge, vertice.getSubject()).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));

    if (vertice.getFilters()
        .stream()
        .anyMatch(filter -> Objects.nonNull(filter.getEdge()))) {
      GraphPatternNotTriples graphPatternNotTriples = GraphPatterns.and(result.toArray(GraphPattern[]::new));

      vertice.getFilters()
          .stream()
          .filter(filter -> Objects.nonNull(filter.getEdge()))
          .forEach(filter -> {
            graphPatternNotTriples.filter(Expressions.equals(Expressions.coalesce(filter.getEdge()
                .getAggregate()
                .getVariable(), Rdf.literalOf(0)), filter.getOperands()
                    .get(0)));
          });

      return singletonList(graphPatternNotTriples);
    }

    return result;
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
          .map(filter -> {
            Variable variable;
            if (Objects.nonNull(edge.getAggregate())) {
              variable = edge.getAggregate()
                  .getVariable();
            } else if (Objects.nonNull(edge.getObject()
                .getSubject())) {
              variable = edge.getObject()
                  .getSubject();
            } else {
              variable = subject;
            }

            return getFilterExpression(filter, variable);
          })
          .collect(Collectors.toList()));
      graphPattern = graphPattern.filter(expression);
    }

    List<GraphPattern> childPatterns = getWherePatterns(edge.getObject());
    graphPattern.and(childPatterns.toArray(new GraphPattern[0]));

    if (Objects.nonNull(edge.getAggregate()) && Objects.equals("COUNT", edge.getAggregate()
        .getType())) {
      graphPattern = GraphPatterns.select(getSelectedForCount(subject, subject, edge).toArray(new Projectable[] {}))
          .where(graphPattern)
          .groupBy(subject)
          .optional(true);
    }
    return singletonList(graphPattern);
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
