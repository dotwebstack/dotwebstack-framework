package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
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

  default Mono<Map<String, Object>> loadSingleRequest(ObjectRequest objectRequest) {
    return Mono.empty();
  }

  default Flux<Map<String, Object>> loadManyRequest(KeyCondition keyCondition, CollectionRequest collectionRequest) {
    return Flux.empty();
  }

  default Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingleRequest(ObjectRequest objectRequest) {
    return Flux.empty();
  }

  default Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadManyRequest(Set<KeyCondition> keyConditions,
      CollectionRequest collectionRequest) {
    return Flux.empty();
  }

  default boolean useRequestApproach() {
    return false;
  }
}
