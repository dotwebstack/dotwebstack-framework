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

  public static final Integer DEFAULT_PAGE = 1;

  public static final Integer DEFAULT_PAGESIZE = 10;

  private final JexlEngine jexlEngine;

  private SubjectQueryBuilder(final QueryEnvironment environment, final JexlEngine jexlEngine) {
    super(environment, Queries.SELECT());
    this.jexlEngine = jexlEngine;
  }

  static SubjectQueryBuilder create(final QueryEnvironment environment,
                                    final JexlEngine jexlEngine) {
    return new SubjectQueryBuilder(environment, jexlEngine);
  }

  String getQueryString(final Map<String, Object> arguments,
                        final GraphQLDirective sparqlDirective) {
    Variable subjectVar = SparqlBuilder.var("s");
    NodeShape nodeShape = environment.getNodeShapeRegistry().get(environment.getObjectType());

    arguments.putIfAbsent("page", DEFAULT_PAGE);
    arguments.putIfAbsent("pageSize", DEFAULT_PAGESIZE);
    MapContext context = new MapContext(arguments);

    JexlExpression limitExpression = jexlEngine.createExpression(DirectiveUtils
        .getStringArgument(Rdf4jDirectives.SPARQL_ARG_LIMIT, sparqlDirective));
    Integer limit = (Integer) limitExpression.evaluate(context);

    JexlExpression offsetExpression = jexlEngine.createExpression(DirectiveUtils
        .getStringArgument(Rdf4jDirectives.SPARQL_ARG_OFFSET, sparqlDirective));
    Integer offset = (Integer) offsetExpression.evaluate(context);

    query.select(subjectVar)
    .where(GraphPatterns
      .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass())))
      .limit(limit)
      .offset(offset);

    return query.getQueryString();
  }
}
