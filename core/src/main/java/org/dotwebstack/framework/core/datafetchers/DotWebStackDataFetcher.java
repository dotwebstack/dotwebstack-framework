package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public interface DotWebStackDataFetcher<T> extends DataFetcher<T> {

  boolean supports(DataFetchingEnvironment environment);

  DataFetcherType getType();

}
