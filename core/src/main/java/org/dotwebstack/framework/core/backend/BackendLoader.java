package org.dotwebstack.framework.core.backend;

import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BackendLoader {

  Mono<Map<String, Object>> loadSingle();

  Flux<Map<String, Object>> loadMany();
}
