package org.dotwebstack.framework.backend.rdf4j.query;

import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private SubjectQueryBuilder(QueryEnvironment environment) {
    super(environment, Queries.SELECT());
  }

  static SubjectQueryBuilder create(QueryEnvironment environment) {
    return new SubjectQueryBuilder(environment);
  }

  String getQueryString() {
    Variable subjectVar = SparqlBuilder.var("s");
    NodeShape nodeShape = environment.getNodeShapeRegistry().get(environment.getObjectType());

    query.select(subjectVar)
        .where(GraphPatterns
            .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass())));

    return query.getQueryString();
  }

}
