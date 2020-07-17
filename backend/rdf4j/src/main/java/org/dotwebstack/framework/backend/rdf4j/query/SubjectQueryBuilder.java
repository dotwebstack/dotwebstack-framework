package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLDirective;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.helper.QueryBuilderHelper;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
import org.dotwebstack.framework.backend.rdf4j.query.model.OrderBy;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private final JexlHelper jexlHelper;

  private final NodeShape nodeShape;

  private final VerticeFactory constructVerticeFactory;

  private SubjectQueryBuilder(@NonNull QueryEnvironment environment, @NonNull JexlEngine jexlEngine,
      @NonNull VerticeFactory constructVerticeFactory) {
    super(environment, Queries.SELECT());
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.nodeShape = this.environment.getNodeShapeRegistry()
        .get(this.environment.getObjectType());
    this.constructVerticeFactory = constructVerticeFactory;
  }

  static SubjectQueryBuilder create(@NonNull QueryEnvironment environment, @NonNull JexlEngine jexlEngine,
      @NonNull VerticeFactory constructVerticeFactory) {
    return new SubjectQueryBuilder(environment, jexlEngine, constructVerticeFactory);
  }

  String getQueryString(final Map<String, Object> arguments, final GraphQLDirective sparqlDirective,
      List<FilterRule> filterRules, List<OrderBy> orderBys) {
    final MapContext context = new MapContext(arguments);

    Vertice root = constructVerticeFactory.buildSelectQuery(nodeShape, filterRules, orderBys, query);

    query.select(root.getSubject())
        .where(QueryBuilderHelper.buildWhereTriples(root)
            .toArray(new GraphPattern[] {}));

    getLimitFromContext(context, sparqlDirective).ifPresent(query::limit);
    getOffsetFromContext(context, sparqlDirective).ifPresent(query::offset);
    root.getOrderables()
        .forEach(query::orderBy);

    if (distinctQuery(sparqlDirective) || distinctQuery(nodeShape)) {
      return this.query.distinct()
          .getQueryString();
    }
    return this.query.getQueryString();
  }

  private boolean distinctQuery(NodeShape nodeShape) {
    return getPropertyShapeSet(nodeShape).stream()
        .anyMatch(ps -> ps.getNode() != null && ps.getMaxCount() != null && ps.getMaxCount() > 1);
  }

  private boolean distinctQuery(@NonNull GraphQLDirective sparqlDirective) {
    return Optional.of(sparqlDirective)
        .map(directive -> directive.getArgument(Rdf4jDirectives.SPARQL_ARG_DISTINCT))
        .map(argument -> (Boolean) argument.getValue())
        .orElse(false);
  }

  private Set<PropertyShape> getPropertyShapeSet(NodeShape nodeShape) {
    Set<PropertyShape> propertyShapes = new HashSet<>(nodeShape.getPropertyShapes()
        .values());
    propertyShapes.forEach(propertyShape -> {
      if (!propertyShapes.contains(propertyShape) && propertyShape.getNode() != null) {
        propertyShapes.addAll(getPropertyShapeSet(propertyShape.getNode()));
      }
    });
    return propertyShapes;
  }

  Optional<Integer> getLimitFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> limitOptional = this.jexlHelper.evaluateDirectiveArgument(sparqlDirective,
        Rdf4jDirectives.SPARQL_ARG_LIMIT, context, Integer.class);
    limitOptional.ifPresent(limit -> {
      if (limit < 1) {
        throw illegalArgumentException("An error occured in the limit expression evaluation");
      }
    });
    return limitOptional;
  }

  Optional<Integer> getOffsetFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    Optional<Integer> offsetOptional = this.jexlHelper.evaluateDirectiveArgument(sparqlDirective,
        Rdf4jDirectives.SPARQL_ARG_OFFSET, context, Integer.class);

    offsetOptional.ifPresent(offset -> {
      if (offset < 0) {
        throw illegalArgumentException("An error occured in the offset expression evaluation");
      }
    });
    return offsetOptional;
  }
}
