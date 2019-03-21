package org.dotwebstack.framework.core.backend;

import graphql.language.FieldDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;

public interface Backend {

  DataFetcher getObjectFetcher(GraphQLFieldDefinition fieldDefinition);

  DataFetcher getPropertyFetcher(GraphQLFieldDefinition fieldDefinition);

}
