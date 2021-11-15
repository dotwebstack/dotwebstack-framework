package org.dotwebstack.framework.service.openapi.param;

import java.util.Map;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ParameterResolver {

  Mono<Map<String, Object>> resolveParameters(ServerRequest serverRequest);
}
