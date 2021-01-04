package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(GraphQLObjectType objectType) {
    return false;
  }

  Mono<Map<String, Object>> loadSingle(Object key, LoadEnvironment environment);

  Flux<Tuple2<Object, Map<String, Object>>> batchLoadSingle(Flux<Object> keys, LoadEnvironment environment);

  Flux<Map<String, Object>> loadMany(Object key, LoadEnvironment environment);

  Flux<Flux<Map<String, Object>>> batchLoadMany(List<Object> keys, LoadEnvironment environment);
}
