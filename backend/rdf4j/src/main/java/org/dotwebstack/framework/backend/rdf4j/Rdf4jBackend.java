package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.SelectOneFetcher;
import org.dotwebstack.framework.core.backend.Backend;
import org.eclipse.rdf4j.repository.Repository;

@RequiredArgsConstructor
final class Rdf4jBackend implements Backend {

  static final String LOCAL_BACKEND_NAME = "local";

  private final Repository repository;

  @Override
  public DataFetcher getObjectFetcher(GraphQLFieldDefinition fieldDefinition) {
    return new SelectOneFetcher(repository);
  }

  @Override
  public DataFetcher getPropertyFetcher(GraphQLFieldDefinition fieldDefinition) {
    return new BindingSetFetcher(fieldDefinition.getName());
  }

}
