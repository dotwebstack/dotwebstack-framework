package org.dotwebstack.framework.backend.rdf4j.expression;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Getter
@Setter
@Builder
public class ExpressionContext {

  FilterOperator operator;

  List<Operand> operands;

  public Expression<?> getExpression(Variable subject) {
    List<Expression<?>> expressions = operands.stream()
        .map(operand -> ExpressionHelper.getExpressionFromOperator(subject, operator, operand))
        .collect(Collectors.toList());
    return ExpressionHelper.joinExpressions(FilterJoinType.OR, null, expressions);
  }
}
