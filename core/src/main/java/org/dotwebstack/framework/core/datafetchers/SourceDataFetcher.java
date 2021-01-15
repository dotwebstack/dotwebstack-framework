package org.dotwebstack.framework.core.datafetchers;

import org.dotwebstack.framework.core.converters.CoreConverterRouter;

@Deprecated
public abstract class SourceDataFetcher implements CoreDataFetcher<Object> {

  protected final CoreConverterRouter converterRouter;

  public SourceDataFetcher(CoreConverterRouter converterRouter) {
    this.converterRouter = converterRouter;
  }

  @Override
  public DataFetcherType getType() {
    return DataFetcherType.SOURCE;
  }

}
