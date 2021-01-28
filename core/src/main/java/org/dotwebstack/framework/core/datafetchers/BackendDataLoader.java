package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface BackendDataLoader {

  default boolean supports(TypeConfiguration<?> typeConfiguration) {
    return false;
  }

  Mono<Map<String, Object>> loadSingle(Filter filter, LoadEnvironment environment);

  Flux<Tuple2<Filter, Map<String, Object>>> batchLoadSingle(Set<Filter> filters, LoadEnvironment environment);

  Flux<Map<String, Object>> loadMany(Filter filter, LoadEnvironment environment);

  Flux<GroupedFlux<Filter, Map<String, Object>>> batchLoadMany(Set<Filter> filters, LoadEnvironment environment);
}
