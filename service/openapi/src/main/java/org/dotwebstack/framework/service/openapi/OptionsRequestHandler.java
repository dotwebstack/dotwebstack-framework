package org.dotwebstack.framework.service.openapi;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class OptionsRequestHandler implements HandlerFunction<ServerResponse> {

  private List<HttpMethod> allowMethods;

  OptionsRequestHandler(@NonNull List<HttpMethod> allowMethods) {
    this.allowMethods = allowMethods;
  }

  @Override
  public Mono<ServerResponse> handle(@NonNull ServerRequest serverRequest) {
    return ServerResponse.ok()
        .header("Allow", String.format("OPTIONS, %s", allowMethods.stream()
            .map(Enum<HttpMethod>::name)
            .collect(Collectors.joining(", "))))
        .build();
  }
}
