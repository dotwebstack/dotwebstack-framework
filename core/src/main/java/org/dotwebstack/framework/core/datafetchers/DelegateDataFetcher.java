package org.dotwebstack.framework.core.datafetchers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.function.Supplier;

public abstract class DelegateDataFetcher implements CoreDataFetcher<Object> {

  protected DataFetcher<Object> getDelegate(DataFetchingEnvironment dataFetchingEnvironment) {
    if (dataFetchingEnvironment.getLocalContext() != null) {
      Supplier<DataFetcher<Object>> supplier = dataFetchingEnvironment.getLocalContext();
      return supplier.get();
    }

    throw illegalArgumentException("dataFetchingEnvironment doesn't contain delegate supplier!");
  }

  @Override
  public DataFetcherType getType() {
    return DataFetcherType.DELEGATE;
  }
}
