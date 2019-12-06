package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Objects.isNull;
import static org.dotwebstack.framework.backend.rdf4j.helper.FieldPathHelper.getFieldDefinitions;
import static org.dotwebstack.framework.core.directives.FilterOperator.EQ;
import static org.dotwebstack.framework.core.directives.FilterOperator.GT;
import static org.dotwebstack.framework.core.directives.FilterOperator.GTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LANGUAGE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LT;
import static org.dotwebstack.framework.core.directives.FilterOperator.LTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.NE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction.LANG;

import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

public class FilterHelper {

  private static final ImmutableMap<FilterOperator, BiFunction<Variable, Operand, Expression<?>>> MAP =
      ImmutableMap.<FilterOperator, BiFunction<Variable, Operand, Expression<?>>>builder()
          .put(EQ, Expressions::equals)
          .put(NE, Expressions::notEquals)
          .put(LT, Expressions::lt)
          .put(LTE, Expressions::lte)
          .put(GT, Expressions::gt)
          .put(GTE, Expressions::gte)
          .build();

  private FilterHelper() {}

  static Expression<?> getExpressionFromOperator(Variable subject, FilterOperator operator, Operand operand) {
    // filtering with a language tag will result in "FILTER LANG( ?x0 ) = 'en'" instead of "FILTER ?x0 =
    // 'something'"
    if (LANGUAGE.equals(operator)) {
      return Expressions.equals(Expressions.function(LANG, subject), operand);
    }

    BiFunction<Variable, Operand, Expression<?>> function = MAP.get(operator);

    if (function == null) {
      throw unsupportedOperationException("Invalid operator '{}' in filter directive for '{}'", operator, subject);
    }

    return function.apply(subject, operand);
  }

  static Expression<?> joinExpressions(FilterJoinType joinType, Expression<?> joinedExpression,
      List<Expression<?>> expressions) {
    Expression<?> current = expressions.remove(0);
    Expression<?> usedExpression;

    if (isNull(joinedExpression)) {
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

  static Operand getOperand(NodeShape nodeShape, String field, String filterString, String tagLanguage) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(field);

    if (Optional.of(nodeShape.getPropertyShape(field))
        .stream()
        .map(PropertyShape::getNodeKind)
        .anyMatch(SHACL.IRI::equals)) {
      return Rdf.iri(filterString);
    }

    if (Objects.equals(RDF.LANGSTRING, propertyShape.getDatatype())) {
      return Rdf.literalOfLanguage(filterString, tagLanguage);
    }

    if (isNull(propertyShape.getDatatype())) {
      return Rdf.literalOf((Integer.parseInt(filterString)));
    }

    return Rdf.literalOfType(filterString, Rdf.iri(propertyShape.getDatatype()
        .stringValue()));
  }

  public static List<GraphQLFieldDefinition> getFilterRulePath(GraphQLObjectType objectType,
      GraphQLDirectiveContainer container) {
    String path = Optional.of(container)
        .map(con -> container.getDirective(CoreDirectives.FILTER_NAME))
        .map(dc -> dc.getArgument(CoreDirectives.FILTER_ARG_FIELD))
        .map(arg -> (String) arg.getValue())
        .orElse(container.getName());

    return getFieldDefinitions(objectType, path);
  }
}
