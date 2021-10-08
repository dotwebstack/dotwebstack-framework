package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.helpers.TypeHelper.isListType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isSubscription;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
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

    var isSubscription = isSubscription(environment.getOperationDefinition());

    if (isSubscription || isListType(environment.getFieldType())) {
      var collectionRequest = requestFactory.createCollectionRequest(environment);
      var result = backendLoader.loadMany(collectionRequest);

      if (isSubscription) {
        return result;
      }

      return result.collectList()
          .toFuture();
    }

    var objectRequest = requestFactory.createObjectRequest(environment);

    return backendLoader.loadSingle(objectRequest)
        .toFuture();
  }
}
