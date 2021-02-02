package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(TypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<Map<String, Object>> loadSingle(KeyCondition keyCondition, LoadEnvironment environment);

  Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> keyConditions,
      LoadEnvironment environment);

  Flux<Map<String, Object>> loadMany(KeyCondition keyCondition, LoadEnvironment environment);

  Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(Set<KeyCondition> keyConditions,
      LoadEnvironment environment);
}
