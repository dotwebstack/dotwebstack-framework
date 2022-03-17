package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getAdditionalData;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class CustomValueDataFetcher implements DataFetcher<Object> {

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public CustomValueDataFetcher(CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    var fieldDefinition = environment.getFieldDefinition()
        .getDefinition();

    return getAdditionalData(fieldDefinition, CUSTOM_FIELD_VALUEFETCHER)
        .map(customValueFetcher -> customValueFetcherDispatcher.fetch(customValueFetcher, environment.getSource()));
  }
}
