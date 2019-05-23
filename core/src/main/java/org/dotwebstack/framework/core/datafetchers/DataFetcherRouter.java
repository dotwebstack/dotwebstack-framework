package org.dotwebstack.framework.core.datafetchers;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.springframework.stereotype.Component;

@Component
public class DataFetcherRouter implements DataFetcher<Object> {

  private final Set<CoreDataFetcher<Object>> dataFetchers;

  public DataFetcherRouter(final Set<CoreDataFetcher<Object>> dataFetchers) {
    this.dataFetchers = dataFetchers;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    CoreDataFetcher<Object> sourceDataFetcher =
        findSupportedDataFetcher(environment, DataFetcherType.SOURCE).orElseThrow(() -> ExceptionHelper
            .illegalArgumentException("No source data fetcher is available for the given environment"));

    Optional<CoreDataFetcher<Object>> delegateDataFetcher =
        findSupportedDataFetcher(environment, DataFetcherType.DELEGATE);

    if (delegateDataFetcher.isPresent()) {
      DataFetchingEnvironment newEnvironment =
          newDataFetchingEnvironment(environment).localContext((Supplier) () -> sourceDataFetcher)
              .build();
      return delegateDataFetcher.get()
          .get(newEnvironment);
    }

    return sourceDataFetcher.get(environment);
  }

  private Optional<CoreDataFetcher<Object>> findSupportedDataFetcher(DataFetchingEnvironment environment,
      DataFetcherType type) {
    return dataFetchers.stream()
        .filter(dataFetcher -> type.equals(dataFetcher.getType()))
        .filter(dataFetcher -> dataFetcher.supports(environment))
        .findFirst();
  }
}
