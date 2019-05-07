package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.GraphQLDirective;

import java.util.Arrays;
import java.util.List;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private static final Variable SUBJECT_VAR = SparqlBuilder.var("s");

  private final JexlHelper jexlHelper;

  private final NodeShape nodeShape;

  private SubjectQueryBuilder(final QueryEnvironment environment, final JexlEngine jexlEngine) {
    super(environment, Queries.SELECT());
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.nodeShape = this.environment.getNodeShapeRegistry().get(this.environment.getObjectType());
  }

  static SubjectQueryBuilder create(final QueryEnvironment environment,
                                    final JexlEngine jexlEngine) {
    return new SubjectQueryBuilder(environment, jexlEngine);
  }

  String getQueryString(final Map<String, Object> arguments,
                        final GraphQLDirective sparqlDirective) {
    final MapContext context = new MapContext(arguments);

    this.query.select(SUBJECT_VAR)
        .where(GraphPatterns.tp(SUBJECT_VAR, ns(RDF.TYPE), ns(this.nodeShape.getTargetClass())));

    getLimitFromContext(context, sparqlDirective).ifPresent(query::limit);
    getOffsetFromContext(context, sparqlDirective).ifPresent(query::offset);
    getOrderByFromContext(context, sparqlDirective).ifPresent(this::buildOrderBy);

    return this.query.getQueryString();
  }

  private void buildOrderBy(List<OrderContext> orderContexts) {
    orderContexts.forEach(orderContext -> {
      query.orderBy(orderContext.getOrderable());
      // add the order property to the query
      query.where(GraphPatterns.tp(SUBJECT_VAR, orderContext.getPropertyShape().getPath(),
          SparqlBuilder.var(orderContext.getField())));
    });
  }

  @SuppressWarnings({"unchecked","rawtypes"})
  Optional<List<OrderContext>> getOrderByFromContext(MapContext context,
                                                     GraphQLDirective sparqlDirective) {
    Optional<List> orderByObject = jexlHelper.evaluateDirectiveArgument(
        Rdf4jDirectives.SPARQL_ARG_ORDER_BY, sparqlDirective, context, List.class);

    if (!orderByObject.isPresent()) {
      return Optional.empty();
    } else {
      List<Map<String, String>> orderByList = (List<Map<String, String>>) orderByObject.get();

      return Optional.of(orderByList.stream().map(this::getOrderContext)
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
      throw new IllegalArgumentException(
          String.format("Not possible to order by field %s, it does not exist on %s.",
              field, nodeShape.getIdentifier()));
    }
    return pred;
  }

  Optional<Integer> getLimitFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> limit = this.jexlHelper.evaluateDirectiveArgument(
        Rdf4jDirectives.SPARQL_ARG_LIMIT, sparqlDirective, context, Integer.class);
    limit.ifPresent(i -> {
      if (i < 1) {
        throw new IllegalArgumentException("An error occured in the limit expression evaluation");
      }
    });
    return limit;
  }

  Optional<Integer> getOffsetFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> offset = this.jexlHelper.evaluateDirectiveArgument(
        Rdf4jDirectives.SPARQL_ARG_OFFSET, sparqlDirective, context, Integer.class);

    offset.ifPresent(i -> {
      if (i < 0) {
        throw new IllegalArgumentException("An error occured in the offset expression evaluation");
      }
    });
    return offset;
  }
}
