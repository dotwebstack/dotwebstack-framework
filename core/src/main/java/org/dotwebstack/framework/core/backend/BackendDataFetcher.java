package org.dotwebstack.framework.core.backend;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Map;

class BackendDataFetcher implements DataFetcher<Object> {

  private final BackendLoader backendLoader;

  private final BackendRequestFactory requestFactory;

  public BackendDataFetcher(BackendLoader backendLoader, BackendRequestFactory requestFactory) {
    this.backendLoader = backendLoader;
    this.requestFactory = requestFactory;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();

    var fieldName = environment.getField()
        .getName();

    // Data was eager-loaded by parent
    if (source != null && source.containsKey(fieldName)) {
      return source.get(fieldName);
    }

    if (isListType(environment.getFieldType())) {
      var collectionRequest = requestFactory.createCollectionRequest(environment);

      return backendLoader.loadMany(collectionRequest)
          .collectList()
          .toFuture();
    }

    var objectRequest = requestFactory.createObjectRequest(environment);

    return backendLoader.loadSingle(objectRequest)
        .toFuture();
  }

  // TODO move to utils class
  private static boolean isListType(GraphQLOutputType type) {
    return GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(type));
  }
}
