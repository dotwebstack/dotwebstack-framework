package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static java.util.Objects.isNull;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FieldPathHelper.getFieldDefinitions;
import static org.dotwebstack.framework.core.directives.FilterOperator.EQ;
import static org.dotwebstack.framework.core.directives.FilterOperator.GT;
import static org.dotwebstack.framework.core.directives.FilterOperator.GTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LANGUAGE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LT;
import static org.dotwebstack.framework.core.directives.FilterOperator.LTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.NE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction.CONTAINS;
import static org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction.LANG;
import static org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction.LCASE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.ArgumentFieldMapping;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Filter;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;

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

  public static Expression<?> buildExpressionFromOperator(@NonNull Variable subject, @NonNull FilterOperator operator,
      @NonNull Operand operand) {
    // filtering with en language tag will result in "FILTER LANG( ?x0 ) = 'en'"
    if (LANGUAGE.equals(operator)) {
      return Expressions.equals(Expressions.function(LANG, subject), operand);
    }
    // filtering with contains will result in "FILTER CONTAINS( ?x0 ,'str')
    if (FilterOperator.CONTAINS.equals(operator)) {
      return Expressions.function(CONTAINS, subject, operand);
    }
    // case-insensitive filtering
    // with 'iContains' will result in "FILTER CONTAINS( LCASE(?x0) , LCASE('str'))
    if (FilterOperator.ICONTAINS.equals(operator)) {
      return Expressions.function(CONTAINS, Expressions.function(LCASE, subject), Expressions.function(LCASE, operand));
    }

    BiFunction<Variable, Operand, Expression<?>> function = MAP.get(operator);

    if (function == null) {
      throw unsupportedOperationException("Invalid operator '{}' in filter directive for '{}'", operator, subject);
    }

    return function.apply(subject, operand);
  }

  public static Expression<?> joinExpressions(@NonNull FilterJoinType joinType, Expression<?> joinedExpression,
      @NonNull List<Expression<?>> expressions) {
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

  public static FilterRule buildFilterRule(@NonNull ArgumentFieldMapping mapping) {
    return buildFilterRule(mapping.getArgumentValue(), mapping.getFieldPath());
  }

  public static FilterRule buildFilterRule(Object argumentValue, FieldPath fieldPath) {
    return FilterRule.builder()
        .fieldPath(fieldPath)
        .value(argumentValue)
        .build();
  }

  public static RdfValue buildOperands(@NonNull NodeShape nodeShape, @NonNull FilterRule filterRule, String language,
      String filterString) {
    if (filterRule.getFieldPath()
        .isResource()) {
      return Rdf.iri(filterString);
    }
    String field = filterRule.getFieldPath()
        .last()
        .map(GraphQLFieldDefinition::getName)
        .orElse(null);

    PropertyShape propertyShape = nodeShape.getPropertyShape(field);

    if (Optional.of(propertyShape)
        .stream()
        .map(PropertyShape::getNodeKind)
        .anyMatch(SHACL.IRI::equals)) {
      return Rdf.iri(filterString);
    }

    if (Objects.equals(RDF.LANGSTRING, propertyShape.getDatatype())) {
      return Rdf.literalOfLanguage(filterString, language);
    }

    if (isNull(propertyShape.getDatatype())) {
      return Rdf.literalOf((Integer.parseInt(filterString)));
    }

    return Rdf.literalOfType(filterString, Rdf.iri(propertyShape.getDatatype()
        .stringValue()));
  }

  public static List<GraphQLFieldDefinition> getFilterRulePath(@NonNull GraphQLObjectType objectType,
      @NonNull GraphQLDirectiveContainer container) {
    String path = Optional.of(container)
        .map(con -> container.getDirective(CoreDirectives.FILTER_NAME))
        .map(dc -> dc.getArgument(CoreDirectives.FILTER_ARG_FIELD))
        .map(arg -> (String) arg.getValue())
        .orElse(container.getName());

    return getFieldDefinitions(objectType, path);
  }

  public static void addLanguageFilter(@NonNull Edge edge, @NonNull PropertyShape propertyShape, String language) {
    if (Objects.equals(RDF.LANGSTRING, propertyShape.getDatatype())) {
      Filter languageFilter = buildLanguageFilter(language);
      edge.getObject()
          .getFilters()
          .add(languageFilter);
    }
  }

  private static Filter buildLanguageFilter(String language) {
    return Filter.builder()
        .operator(FilterOperator.LANGUAGE)
        .operands(ImmutableList.of(Rdf.literalOf(language)))
        .build();
  }
}
