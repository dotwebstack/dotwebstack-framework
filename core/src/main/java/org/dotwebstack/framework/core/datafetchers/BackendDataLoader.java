package org.dotwebstack.framework.core.datafetchers;

import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(TypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<DataLoaderResult> loadSingle(Object key, LoadEnvironment environment);

  Flux<Tuple2<Object, DataLoaderResult>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment);

  Flux<DataLoaderResult> loadMany(Object key, LoadEnvironment environment);

  Flux<Flux<DataLoaderResult>> batchLoadMany(List<Object> keys, LoadEnvironment environment);
}
