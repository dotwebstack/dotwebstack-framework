package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.core.directives.FilterOperator.EQ;
import static org.dotwebstack.framework.core.directives.FilterOperator.GT;
import static org.dotwebstack.framework.core.directives.FilterOperator.GTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LT;
import static org.dotwebstack.framework.core.directives.FilterOperator.LTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.NE;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

public class FilterHelper {

  private static final ImmutableMap<FilterOperator, BiFunction<Variable, Operand, Expression<?>>> MAP = ImmutableMap
      .<FilterOperator, BiFunction<Variable, Operand, Expression<?>>>builder()
      .put(EQ, Expressions::equals)
      .put(NE, Expressions::notEquals)
      .put(LT, Expressions::lt)
      .put(LTE, Expressions::lte)
      .put(GT, Expressions::gt)
      .put(GTE, Expressions::gte)
      .build();

  private FilterHelper() {}

  static Expression<?> getExpressionFromOperator(Variable subject, FilterOperator operator, Operand operand) {
    BiFunction<Variable, Operand, Expression<?>> function = MAP.get(operator);

    if (function == null) {
      throw ExceptionHelper.unsupportedOperationException("Invalid operator '{}' in filter directive for '{}'",
          operator, subject);
    }

    return function.apply(subject, operand);
  }

  public static Expression<?> joinExpressions(FilterJoinType joinType, Expression<?> joinedExpression,
      List<Expression<?>> expressions) {
    Expression<?> current = expressions.remove(0);
    Expression<?> usedExpression;

    if (Objects.isNull(joinedExpression)) {
      usedExpression = current;
    } else {
      Operand[] operands = new Expression<?>[] {current, joinedExpression};
      usedExpression = FilterJoinType.AND.equals(joinType) ? Expressions.and(operands) : Expressions.or(operands);
    }

    if (!expressions.isEmpty()) {
      return joinExpressions(joinType, usedExpression, expressions);
    }

    return usedExpression;
  }

  public static Operand getOperand(NodeShape nodeShape, String field, Object value) {
    String string = ObjectHelper.cast(String.class, value);

    if (Objects.isNull(nodeShape.getPropertyShape(field))) {
      throw ExceptionHelper.unsupportedOperationException("Property shape for '{}' does not exist on node shape '{}'",
          field, nodeShape);
    }

    if (nodeShape.getPropertyShape(field)
        .getNodeKind()
        .equals(SHACL.IRI)) {
      return Rdf.iri(string);
    }

    return Rdf.literalOfType(string, nodeShape.getPropertyShape(field)
        .getDatatype());
  }
}
