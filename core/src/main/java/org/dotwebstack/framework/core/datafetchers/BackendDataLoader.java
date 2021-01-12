package org.dotwebstack.framework.core.datafetchers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(AbstractTypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment);

  Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Set<Object> keys, LoadEnvironment environment);

  Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment);

  Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment);
}
