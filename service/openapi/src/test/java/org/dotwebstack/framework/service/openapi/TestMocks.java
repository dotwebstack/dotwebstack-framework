package org.dotwebstack.framework.service.openapi;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;

public class TestMocks {

  private TestMocks() {}

  public static ServerRequest mockRequest(HttpMethod httpMethod, String path) {
    var serverHttpRequest = MockServerHttpRequest.method(httpMethod, path)
        .build();

    return ServerRequest.create(MockServerWebExchange.from(serverHttpRequest), List.of());
  }

  public static ServerRequest mockRequest(HttpMethod httpMethod, MediaType mediaType, String path) {
    var serverHttpRequest = MockServerHttpRequest.method(httpMethod, path)
        .accept(mediaType)
        .build();

    return ServerRequest.create(MockServerWebExchange.from(serverHttpRequest), List.of());
  }
}
