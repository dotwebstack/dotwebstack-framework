package org.dotwebstack.framework.service.http;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
class OpenApiConfiguration {

  private static final String FILENAME = "config/model/spec.yml";

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(FILENAME);
  }

  @Bean
  public RouterFunction<ServerResponse> route(OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();
    openApi.getPaths()
        .forEach((name, path) -> {
          if (Objects.nonNull(path.getGet())) {
            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)), new CoreRequestHandler()));
          }
        });
    return routerFunctions.build();
  }
}
