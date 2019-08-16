package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;

public interface RepositoryAdapter {
  TupleQuery prepareTupleQuery(String repositoryId, DataFetchingEnvironment environment, String query);

  GraphQuery prepareGraphQuery(String repositoryId, DataFetchingEnvironment environment, String query);

  boolean supports(String repositoryId);
}
