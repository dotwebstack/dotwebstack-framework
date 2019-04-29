package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  public static final Integer DEFAULT_LIMIT = 10;

  public static final Integer DEFAULT_OFFSET = 1;

  private SubjectQueryBuilder(QueryEnvironment environment) {
    super(environment, Queries.SELECT());
  }

  static SubjectQueryBuilder create(QueryEnvironment environment) {
    return new SubjectQueryBuilder(environment);
  }

  String getQueryString(Map arguments) {
    Variable subjectVar = SparqlBuilder.var("s");
    NodeShape nodeShape = environment.getNodeShapeRegistry().get(environment.getObjectType());

    query.select(subjectVar)
        .where(GraphPatterns
            .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass())))
    .limit(getLimit(arguments))
    .offset(getOffset(arguments));

    return query.getQueryString();
  }

  private Integer getOffset(Map arguments) {
    Integer limit = getLimit(arguments);
    Integer offset = DEFAULT_OFFSET;

    if (arguments.containsKey("page")) {
      offset = (((Integer) arguments.get("page") - 1) * limit);
    }

    return offset;
  }

  private Integer getLimit(Map arguments) {
    Integer limit = DEFAULT_LIMIT;
    if (arguments.containsKey("pageSize")) {
      limit = (Integer) arguments.get("pageSize");
    }

    return limit;
  }

}
