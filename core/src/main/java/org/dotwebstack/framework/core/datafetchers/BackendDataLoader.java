package org.dotwebstack.framework.core.datafetchers;

import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(TypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<DataLoaderResult> loadSingle(Filter filter, LoadEnvironment environment);

  Flux<Tuple2<Filter, DataLoaderResult>> batchLoadSingle(Set<Filter> filters, LoadEnvironment environment);

  Flux<DataLoaderResult> loadMany(Filter filter, LoadEnvironment environment);

  Flux<Flux<DataLoaderResult>> batchLoadMany(List<Filter> filters, LoadEnvironment environment);
}
