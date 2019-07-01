package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.directives.FilterOperator.EQ;
import static org.dotwebstack.framework.core.directives.FilterOperator.GT;
import static org.dotwebstack.framework.core.directives.FilterOperator.GTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.LT;
import static org.dotwebstack.framework.core.directives.FilterOperator.LTE;
import static org.dotwebstack.framework.core.directives.FilterOperator.NE;

import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
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
      Map<GraphQLDirectiveContainer, Object> filterMapping) {
    final MapContext context = new MapContext(arguments);

    this.query.select(SUBJECT_VAR);

    TriplePattern whereSubjectType = GraphPatterns.tp(SUBJECT_VAR, RDF.TYPE, Rdf.iri(this.nodeShape.getTargetClass()));
    whereBuilder.put(whereSubjectType.getQueryString(), whereSubjectType);

    getLimitFromContext(context, sparqlDirective).ifPresent(query::limit);
    getOffsetFromContext(context, sparqlDirective).ifPresent(query::offset);
    getOrderByFromContext(context, sparqlDirective).ifPresent(this::buildOrderBy);
    Map<String, Expression<?>> filters = getFilters(filterMapping);

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

      IntStream.range(0, orderContext.getFields()
          .size())
          .forEachOrdered(integer -> {
            OrderContext.Field field = orderContext.getFields()
                .get(integer);
            OrderContext.Field previous = integer == 0 ? null
                : orderContext.getFields()
                    .get(integer - 1);

            Variable subject = previous == null ? SUBJECT_VAR : SparqlBuilder.var(previous.getFieldName());
            Variable object = SparqlBuilder.var(field.getFieldName());

            TriplePattern pattern = GraphPatterns.tp(subject, field.getPropertyShape()
                .getPath()
                .toPredicate(), object);
            whereBuilder.put(pattern.getQueryString(), pattern);
          });
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
    String fieldName = orderMap.get("field");
    String order = orderMap.get("order");

    String[] fields = fieldName.split("\\.");
    List<OrderContext.Field> elements = getContextField(new StringBuilder(), fields, nodeShape);

    // The order variable is the last field in the path.
    Variable orderVar = SparqlBuilder.var(elements.get(elements.size() - 1)
        .getFieldName());
    Orderable orderable = order.equalsIgnoreCase("desc") ? orderVar.desc() : orderVar.asc();
    return new OrderContext(elements, orderable);
  }

  private List<OrderContext.Field> getContextField(StringBuilder pathBuilder, String[] fields, NodeShape nodeShape) {
    String field = fields[0];
    PropertyShape propertyShape = nodeShape.getPropertyShape(field);

    if (propertyShape == null) {
      throw ExceptionHelper.illegalArgumentException(String
          .format("Not possible to order by fieldName %s, it does not exist on %s.", field, nodeShape.getIdentifier()));
    }

    if (!"".equals(pathBuilder.toString())) {
      pathBuilder.append("_");
    }
    pathBuilder.append(field);

    ArrayList<OrderContext.Field> elements = new ArrayList<>();
    elements.add(new OrderContext.Field(pathBuilder.toString(), propertyShape));
    if (fields.length > 1) {
      elements.addAll(getContextField(pathBuilder, ArrayUtils.remove(fields, 0), propertyShape.getNode()));
    }

    return elements;
  }

  Optional<Integer> getLimitFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> limitOptional = this.jexlHelper.evaluateDirectiveArgument(Rdf4jDirectives.SPARQL_ARG_LIMIT,
        sparqlDirective, context, Integer.class);
    limitOptional.ifPresent(limit -> {
      if (limit < 1) {
        throw ExceptionHelper.illegalArgumentException("An error occured in the limit expression evaluation");
      }
    });
    return limitOptional;
  }

  Optional<Integer> getOffsetFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> offsetOptional = this.jexlHelper.evaluateDirectiveArgument(Rdf4jDirectives.SPARQL_ARG_OFFSET,
        sparqlDirective, context, Integer.class);

    offsetOptional.ifPresent(offset -> {
      if (offset < 0) {
        throw ExceptionHelper.illegalArgumentException("An error occured in the offset expression evaluation");
      }
    });
    return offsetOptional;
  }

  @SuppressWarnings("unchecked")
  Map<String, Expression<?>> getFilters(Map<GraphQLDirectiveContainer, Object> filterMapping) {
    Map<String, Expression<?>> expressionMap = new HashMap<>();
    filterMapping.forEach((container, value) -> {
      GraphQLDirective directive = container.getDirective(CoreDirectives.FILTER_NAME);

      GraphQLArgument fieldArgument = directive.getArgument(CoreDirectives.FILTER_ARG_FIELD);
      String field = fieldArgument.getValue() != null ? (String) fieldArgument.getValue() : container.getName();

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
      throw ExceptionHelper.unsupportedOperationException("Invalid operator '{}' in filter directive for '{}'",
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
