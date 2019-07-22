package org.dotwebstack.framework.service.http;

import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  String basequery;

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    return null;
  }
}
