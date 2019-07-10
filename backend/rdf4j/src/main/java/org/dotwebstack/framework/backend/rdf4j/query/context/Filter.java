package org.dotwebstack.framework.backend.rdf4j.query.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

@Data
@Builder
public class Filter {

  private FilterOperator operator;

  @Builder.Default
  private List<Operand> operands = new ArrayList<>();

  public Expression<?> toExpression(Variable subject) {
    List<Expression<?>> expressions = operands.stream()
        .map(operand -> FilterHelper.getExpressionFromOperator(subject, operator, operand))
        .collect(Collectors.toList());
    return FilterHelper.joinExpressions(FilterJoinType.OR, null, expressions);
  }
}
