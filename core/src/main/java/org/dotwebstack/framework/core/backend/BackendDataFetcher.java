package org.dotwebstack.framework.core.backend;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;

class BackendDataFetcher implements DataFetcher<Object> {

  private final BackendLoader backendLoader;

  public BackendDataFetcher(BackendLoader backendLoader) {
    this.backendLoader = backendLoader;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (isListType(environment.getFieldType())) {
      return backendLoader.loadMany()
          .collectList()
          .toFuture();
    }

    return backendLoader.loadSingle()
        .toFuture();
  }

  // TODO move to utils class
  private static boolean isListType(GraphQLOutputType type) {
    return GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(type));
  }
}
