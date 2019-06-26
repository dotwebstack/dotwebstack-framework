package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.directives.FilterOperator.EQ;
import static org.dotwebstack.framework.core.directives.FilterOperator.GT;
import static org.dotwebstack.framework.core.directives.FilterOperator.GTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LT;
import static org.dotwebstack.framework.core.directives.FilterOperator.LTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.NE;

import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.JexlHelper;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
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

  private static final ImmutableMap.Builder<String, BiFunction<String, Operand, Expression<?>>> BUILDER =
      new ImmutableMap.Builder<>();

  static {
    BUILDER.put(EQ.getValue(), (subject, operand) -> Expressions.equals(SparqlBuilder.var(subject), operand));
    BUILDER.put(NE.getValue(), (subject, operand) -> Expressions.notEquals(SparqlBuilder.var(subject), operand));
    BUILDER.put(LT.getValue(), (subject, operand) -> Expressions.lt(SparqlBuilder.var(subject), operand));
    BUILDER.put(LTE.getValue(), (subject, operand) -> Expressions.lte(SparqlBuilder.var(subject), operand));
    BUILDER.put(GT.getValue(), (subject, operand) -> Expressions.gt(SparqlBuilder.var(subject), operand));
    BUILDER.put(GTE.getValue(), (subject, operand) -> Expressions.gte(SparqlBuilder.var(subject), operand));
  }

  private static final ImmutableMap<String, BiFunction<String, Operand, Expression<?>>> MAP = BUILDER.build();

  private final JexlHelper jexlHelper;

  private final NodeShape nodeShape;

  private final ImmutableMap.Builder<String, GraphPattern> whereBuilder = ImmutableMap.builder();

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
      Map<GraphQLDirectiveContainer, Object> FilterMapping) {
    final MapContext context = new MapContext(arguments);

    this.query.select(SUBJECT_VAR);

    TriplePattern whereSubjectType = GraphPatterns.tp(SUBJECT_VAR, RDF.TYPE, Rdf.iri(this.nodeShape.getTargetClass()));
    whereBuilder.put(whereSubjectType.getQueryString(), whereSubjectType);

    getLimitFromContext(context, sparqlDirective).ifPresent(query::limit);
    getOffsetFromContext(context, sparqlDirective).ifPresent(query::offset);
    getOrderByFromContext(context, sparqlDirective).ifPresent(this::buildOrderBy);
    Map<String, Expression<?>> filters = getFilters(FilterMapping);

    whereBuilder.build()
        .values()
        .forEach(whereStatement -> {
          String object = whereStatement.getQueryString()
              .split(" ")[2];
          if (filters.containsKey(object)) {
            whereStatement = whereStatement.filter(filters.get(object));
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
      List<Map<String, String>> orderByList = orderByObject.get();

      return Optional.of(orderByList.stream()
          .map(this::getOrderContext)
          .collect(Collectors.toList()));
    }
  }

  private OrderContext getOrderContext(Map<String, String> orderMap) {
    String field = orderMap.get("field");
    String order = orderMap.get("order");

    Variable var = SparqlBuilder.var(field);
    PropertyShape propertyShape = getPropertyShapeForField(field);

    Orderable orderable = order.equalsIgnoreCase("desc") ? var.desc() : var.asc();
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

  @SuppressWarnings("unchecked")
  Map<String, Expression<?>> getFilters(Map<GraphQLDirectiveContainer, Object> filterMapping) {
    Map<String, Expression<?>> expressionMap = new HashMap<>();
    filterMapping.forEach((container, value) -> {
      GraphQLDirective directive = container.getDirective(CoreDirectives.FILTER_NAME);
      String field = (String) directive.getArgument(CoreDirectives.FILTER_ARG_FIELD)
          .getValue();
      Variable fieldVar = SparqlBuilder.var(field);
      String operator = (String) directive.getArgument(CoreDirectives.FILTER_ARG_OPERATOR)
          .getValue();

      FilterJoinType joinType;
      List<Object> list;
      if (value instanceof List) {
        list = ObjectHelper.cast(List.class, value);
        joinType = FilterJoinType.OR;
      } else {
        list = Collections.singletonList(value);
        joinType = FilterJoinType.AND;
      }

      list.forEach(item -> {
        Expression<?> expression = getExpressionFromOperator(field, operator, item);

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

        addExpression(expression, fieldVar, expressionMap, joinType);
      });
    });

    return expressionMap;
  }

  private void addExpression(Expression<?> expression, Variable fieldVar, Map<String, Expression<?>> expressionMap,
      FilterJoinType joinType) {
    if (expressionMap.containsKey(fieldVar.getQueryString())) {
      Operand[] operands = new Expression<?>[] {expressionMap.get(fieldVar.getQueryString()), expression};
      Expression<?> expressions =
          FilterJoinType.OR.equals(joinType) ? Expressions.or(operands) : Expressions.and(operands);
      expressionMap.put(fieldVar.getQueryString(), expressions);
    } else {
      expressionMap.put(fieldVar.getQueryString(), expression);
    }
  }

  private Expression<?> getExpressionFromOperator(String field, String operator, Object value) {
    BiFunction<String, Operand, Expression<?>> function = MAP.get(operator != null ? operator
        : FilterOperator.getDefault()
            .toString());

    if (function == null) {
      throw ExceptionHelper.unsupportedOperationException("Invalid operator '{}' in Filter directive for '{}'",
          operator, field);
    }

    String string = ObjectHelper.cast(String.class, value);

    if (nodeShape.getPropertyShape(field)
        .getNodeKind()
        .equals(SHACL.IRI)) {
      return function.apply(field, Rdf.iri(string));
    }

    return function.apply(field, Rdf.literalOfType(string, nodeShape.getPropertyShape(field)
        .getDatatype()));
  }
}
