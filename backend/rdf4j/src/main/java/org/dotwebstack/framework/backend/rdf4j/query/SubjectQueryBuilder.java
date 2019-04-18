package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.GraphQLObjectType;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

class SubjectQueryBuilder extends AbstractQueryBuilder<SelectQuery> {

  private SubjectQueryBuilder(GraphQLObjectType objectType, NodeShapeRegistry nodeShapeRegistry,
      Map<String, String> prefixMap) {
    super(objectType, nodeShapeRegistry, prefixMap, Queries.SELECT());
  }

  static SubjectQueryBuilder create(GraphQLObjectType objectType,
      NodeShapeRegistry nodeShapeRegistry, Map<String, String> prefixMap) {
    return new SubjectQueryBuilder(objectType, nodeShapeRegistry, prefixMap);
  }

  String getQueryString() {
    Variable subjectVar = SparqlBuilder.var("s");
    NodeShape nodeShape = nodeShapeRegistry.get(objectType);

    query.select(subjectVar)
        .where(GraphPatterns
            .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass())));

    return query.getQueryString();
  }

}
