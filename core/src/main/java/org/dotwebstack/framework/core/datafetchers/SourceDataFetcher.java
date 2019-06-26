package org.dotwebstack.framework.core.datafetchers;

import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.converters.CoreConverter;

public abstract class SourceDataFetcher implements CoreDataFetcher<Object> {

  @Override
  public DataFetcherType getType() {
    return DataFetcherType.SOURCE;
  }

  public abstract List<CoreConverter<?>> getConverters();

  public Optional<CoreConverter<?>> getConverter(Object value) {
    return getConverters().stream()
        .filter(converter -> converter.supports(value))
        .findFirst();
  }
}
