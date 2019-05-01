package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.GraphQLDirective;
import java.util.Map;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private static final Variable SUBJECT_VAR = SparqlBuilder.var("s");

  private final JexlEngine jexlEngine;

  private final NodeShape nodeShape;

  private SubjectQueryBuilder(final QueryEnvironment environment, final JexlEngine jexlEngine) {
    super(environment, Queries.SELECT());
    this.jexlEngine = jexlEngine;
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

    Integer limit = getLimitFromContext(context, sparqlDirective);
    if (limit != null) {
      query.limit(limit);
    }

    Integer offset = getOffsetFromContext(context, sparqlDirective);
    if (offset != null) {
      query.offset(offset);
    }

    return this.query.getQueryString();
  }

  Integer getLimitFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    String expressionString = DirectiveUtils
            .getStringArgument(Rdf4jDirectives.SPARQL_ARG_LIMIT, sparqlDirective);

    if (expressionString != null) {
      JexlExpression limitExpression = this.jexlEngine.createExpression(expressionString);
      Object limitObject = limitExpression.evaluate(context);

      if (!(limitObject instanceof Integer)) {
        throw new IllegalArgumentException(("The given limit expression is invalid"));
      }

      Integer limit = (Integer) limitObject;

      if (limit < 1) {
        throw new IllegalArgumentException("The given pageSize is invalid");
      }

      return limit;
    }

    return null;
  }

  Integer getOffsetFromContext(MapContext context, GraphQLDirective sparqlDirective) {
    String expressionString = DirectiveUtils
            .getStringArgument(Rdf4jDirectives.SPARQL_ARG_OFFSET, sparqlDirective);

    if (expressionString != null) {
      JexlExpression offsetExpression = this.jexlEngine.createExpression(expressionString);
      Object offsetObject = offsetExpression.evaluate(context);

      if (!(offsetObject instanceof Integer)) {
        throw new IllegalArgumentException(("The given offset expression is invalid"));
      }

      Integer offset = (Integer) offsetObject;

      if (offset < 0) {
        throw new IllegalArgumentException("The given page is invalid");
      }

      return offset;
    }

    return null;
  }

}
