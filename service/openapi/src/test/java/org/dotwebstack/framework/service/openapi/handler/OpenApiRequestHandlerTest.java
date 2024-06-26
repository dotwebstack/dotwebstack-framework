package org.dotwebstack.framework.service.openapi.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.test.StepVerifier;


class OpenApiRequestHandlerTest {

  private ServerResponse.Context context;

  @BeforeEach
  public void createContext() {
    HandlerStrategies strategies = HandlerStrategies.withDefaults();
    context = new ServerResponse.Context() {

      @NonNull
      @Override
      public List<HttpMessageWriter<?>> messageWriters() {
        return strategies.messageWriters();
      }

      @NonNull
      @Override
      public List<ViewResolver> viewResolvers() {
        return strategies.viewResolvers();
      }
    };
  }

  @Test
  void handle_returnsExpectedServerResponse_whenRequested() {
    var serverHttpRequest = MockServerHttpRequest.method(HttpMethod.GET, "")
        .build();
    var exchange = MockServerWebExchange.from(serverHttpRequest);
    var request = ServerRequest.create(exchange, List.of());

    var responseMono = new OpenApiRequestHandler(TestResources.openApiStream()).handle(request)
        .flatMap(serverResponse -> {
          assertThat(serverResponse.statusCode(), is(HttpStatus.OK));
          assertThat(serverResponse.headers()
              .get("Content-type"), hasItem("text/yaml;charset=utf-8"));
          return serverResponse.writeTo(exchange, context);
        });

    StepVerifier.create(responseMono)
        .verifyComplete();

    StepVerifier.create(exchange.getResponse()
        .getBodyAsString())
        .assertNext(body -> {
          assertThat(body, containsString("openapi: \"3.0.2\""));
          assertThat(body, containsString("""
              description: |
                  This is an API for brewery data.
                  It tells you all about breweries and their beers.
                  And much much more.
              """));
        })
        .verifyComplete();
  }
}
