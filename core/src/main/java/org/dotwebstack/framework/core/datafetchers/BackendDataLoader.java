package org.dotwebstack.framework.core.datafetchers;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.keys.Key;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(TypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<Map<String, Object>> loadSingle(Key key, LoadEnvironment environment);

  Flux<Tuple2<Key, Map<String, Object>>> batchLoadSingle(Flux<Key> keys, LoadEnvironment environment);

  Flux<Map<String, Object>> loadMany(Key key, LoadEnvironment environment);

  Flux<Flux<Map<String, Object>>> batchLoadMany(List<Key> keys, LoadEnvironment environment);
}
