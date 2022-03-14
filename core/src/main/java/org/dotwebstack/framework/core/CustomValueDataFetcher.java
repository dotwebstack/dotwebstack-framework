package org.dotwebstack.framework.core;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;

public class CustomValueDataFetcher implements DataFetcher<Object> {

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public CustomValueDataFetcher(CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    var fieldDefinition = environment.getFieldDefinition()
        .getDefinition();

    var customValueFetcher = fieldDefinition.getAdditionalData()
        .get(GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER);

    return customValueFetcherDispatcher.fetch(customValueFetcher, environment.getSource());
  }
}
