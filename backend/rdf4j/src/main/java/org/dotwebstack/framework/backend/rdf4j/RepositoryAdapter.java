package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.GraphQLFieldDefinition;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;

public interface RepositoryAdapter {
  TupleQuery prepareTupleQuery(String repositoryId, GraphQLFieldDefinition fieldDefinition, String query);

  GraphQuery prepareGraphQuery(String repositoryId, GraphQLFieldDefinition fieldDefinition, String query);

  boolean supports(String repositoryId);
}
