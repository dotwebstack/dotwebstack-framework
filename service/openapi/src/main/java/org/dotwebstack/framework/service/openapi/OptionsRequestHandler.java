package org.dotwebstack.framework.service.openapi;

import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
        .header("Allow", String.format("OPTIONS, %s", StringUtils.join(allowMethods.stream()
            .map(Enum::name)
            .toArray(), ", ")))
        .build();
  }
}
