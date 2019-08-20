package org.dotwebstack.framework.service.openapi;

import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class OptionsRequestHandler implements HandlerFunction<ServerResponse> {

  private HttpMethod allowMethod;

  OptionsRequestHandler(@NonNull HttpMethod allowMethod) {
    this.allowMethod = allowMethod;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return ServerResponse.ok()
        .header("Allow", String.format("OPTIONS,%s", allowMethod.name()))
        .build();
  }
}
