package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.helpers.SparqlFilterHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.JexlHelper;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private static final Variable SUBJECT_VAR = SparqlBuilder.var("s");

  private final JexlHelper jexlHelper;

  private final NodeShape nodeShape;

  private final ImmutableMap.Builder<String, GraphPattern> whereBuilder = ImmutableMap.builder();

  private static final ImmutableMap.Builder<String, BiFunction<String, String, Expression<?>>> BUILDER =
      new ImmutableMap.Builder<>();

  static {
    BUILDER.put("=", (subject, object) -> Expressions.equals(SparqlBuilder.var(subject), Rdf.literalOf(object)));
    BUILDER.put("!=", (subject, object) -> Expressions.notEquals(SparqlBuilder.var(subject), Rdf.literalOf(object)));
    BUILDER.put("<", (subject, object) -> Expressions.lt(SparqlBuilder.var(subject), Rdf.literalOf(object)));
    BUILDER.put("<=", (subject, object) -> Expressions.lte(SparqlBuilder.var(subject), Rdf.literalOf(object)));
    BUILDER.put(">", (subject, object) -> Expressions.gt(SparqlBuilder.var(subject), Rdf.literalOf(object)));
    BUILDER.put(">=", (subject, object) -> Expressions.gte(SparqlBuilder.var(subject), Rdf.literalOf(object)));
  }

  private static final ImmutableMap<String, BiFunction<String, String, Expression<?>>> MAP = BUILDER.build();

  private SubjectQueryBuilder(final QueryEnvironment environment, final JexlEngine jexlEngine) {
    super(environment, Queries.SELECT());
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.nodeShape = this.environment.getNodeShapeRegistry()
        .get(this.environment.getObjectType());
  }

  static SubjectQueryBuilder create(final QueryEnvironment environment, final JexlEngine jexlEngine) {
    return new SubjectQueryBuilder(environment, jexlEngine);
  }

  String getQueryString(final Map<String, Object> arguments, final GraphQLDirective sparqlDirective,
      Map<GraphQLDirectiveContainer, Object> sparqlFilterMapping) {
    final MapContext context = new MapContext(arguments);

    this.query.select(SUBJECT_VAR);

    TriplePattern whereSubjectType = GraphPatterns.tp(SUBJECT_VAR, RDF.TYPE, Rdf.iri(this.nodeShape.getTargetClass()));
    whereBuilder.put(whereSubjectType.getQueryString(), whereSubjectType);

    getLimitFromContext(context, sparqlDirective).ifPresent(query::limit);
    getOffsetFromContext(context, sparqlDirective).ifPresent(query::offset);
    getOrderByFromContext(context, sparqlDirective).ifPresent(this::buildOrderBy);
    Map<String, Expression<?>> filters = getSparqlFilters(sparqlFilterMapping);

    whereBuilder.build()
        .values()
        .forEach(whereStatement -> {
          String key = whereStatement.getQueryString()
              .split(" ")[2];
          if (filters.containsKey(key)) {
            whereStatement = whereStatement.filter(filters.get(key));
          }
          query.where(whereStatement);
        });

    return this.query.getQueryString();
  }

  private void buildOrderBy(List<OrderContext> contexts) {
    contexts.forEach(orderContext -> {
      query.orderBy(orderContext.getOrderable());

      TriplePattern triplePattern = GraphPatterns.tp(SUBJECT_VAR, orderContext.getPropertyShape()
          .getPath()
          .toPredicate(), SparqlBuilder.var(orderContext.getField()));

      whereBuilder.put(triplePattern.getQueryString(), triplePattern);
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  Optional<List<OrderContext>> getOrderByFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<List> orderByObject =
        jexlHelper.evaluateDirectiveArgument(Rdf4jDirectives.SPARQL_ARG_ORDER_BY, sparqlDirective, context, List.class);

    if (!orderByObject.isPresent()) {
      return Optional.empty();
    } else {
      List<Map<String, String>> orderByList = (List<Map<String, String>>) orderByObject.get();

      return Optional.of(orderByList.stream()
          .map(this::getOrderContext)
          .collect(Collectors.toList()));
    }
  }

  private OrderContext getOrderContext(Map<String, String> orderMap) {
    String field = orderMap.get("field");
    String order = orderMap.get("order");

    Variable var = SparqlBuilder.var(field);

    Orderable orderable = order.equalsIgnoreCase("desc") ? var.desc() : var.asc();

    PropertyShape propertyShape = getPropertyShapeForField(field);

    return new OrderContext(field, orderable, propertyShape);
  }

  private PropertyShape getPropertyShapeForField(String field) {
    // get the predicate property shape based on the order property field
    PropertyShape pred = this.nodeShape.getPropertyShape(field);
    if (pred == null) {
      throw new IllegalArgumentException(String.format("Not possible to order by field %s, it does not exist on %s.",
          field, nodeShape.getIdentifier()));
    }
    return pred;
  }

  Optional<Integer> getLimitFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> limit = this.jexlHelper.evaluateDirectiveArgument(Rdf4jDirectives.SPARQL_ARG_LIMIT,
        sparqlDirective, context, Integer.class);
    limit.ifPresent(i -> {
      if (i < 1) {
        throw new IllegalArgumentException("An error occured in the limit expression evaluation");
      }
    });
    return limit;
  }

  Optional<Integer> getOffsetFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> offset = this.jexlHelper.evaluateDirectiveArgument(Rdf4jDirectives.SPARQL_ARG_OFFSET,
        sparqlDirective, context, Integer.class);

    offset.ifPresent(i -> {
      if (i < 0) {
        throw new IllegalArgumentException("An error occured in the offset expression evaluation");
      }
    });
    return offset;
  }

  private Map<String, Expression<?>> getSparqlFilters(Map<GraphQLDirectiveContainer, Object> sparqlFilterMapping) {
    Map<String, Expression<?>> expressionMap = new HashMap<>();
    sparqlFilterMapping.forEach((container, value) -> {
      GraphQLDirective directive = container.getDirective(Rdf4jDirectives.SPARQL_FILTER_NAME);
      String field = (String) directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_FIELD)
          .getValue();
      Variable fieldVar = SparqlBuilder.var(field);
      String operator = (String) directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)
          .getValue();
      Expression<?> expression = getExpressionFromOperator(field, operator, (String) value);

      if (whereBuilder.build()
          .values()
          .stream()
          .noneMatch(statement -> statement.getQueryString()
              .split(" ")[2].equals(fieldVar.getQueryString()))) {
        GraphPattern pattern = GraphPatterns.tp(SUBJECT_VAR, nodeShape.getPropertyShape(field)
            .getPath()
            .toPredicate(), fieldVar);
        whereBuilder.put(pattern.getQueryString(), pattern);
      }

      addExpression(expression, fieldVar, expressionMap);
    });

    return expressionMap;
  }

  private void addExpression(Expression<?> expression, Variable fieldVar, Map<String, Expression<?>> expressionMap) {
    List<Expression<?>> expressions = new ArrayList<>();
    Expression<?> addedExpression;

    if (expressionMap.containsKey(fieldVar.getQueryString())) {
      expressions.add(expressionMap.get(fieldVar.getQueryString()));
      expressions.add(expression);
      addedExpression = Expressions.and(Iterables.toArray((expressions), Expression.class));
    } else {
      addedExpression = expression;
    }

    expressionMap.put(fieldVar.getQueryString(), addedExpression);
  }

  private Expression<?> getExpressionFromOperator(String field, String operator, String value) {
    BiFunction<String, String, Expression<?>> function =
        MAP.get(operator != null ? operator : SparqlFilterHelper.DEFAULT_OPERATOR);

    if (function == null) {
      throw ExceptionHelper.unsupportedOperationException("Invalid operator '{}' in sparqlFilter directive for '{}'",
          operator, field);
    }

    return function.apply(field, value);
  }
}
