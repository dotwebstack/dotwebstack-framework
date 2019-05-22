package org.dotwebstack.framework.core.datafetchers;

public abstract class SourceDataFetcher implements DotWebStackDataFetcher<Object> {

  @Override
  public DataFetcherType getType() {
    return DataFetcherType.SOURCE;
  }
}
