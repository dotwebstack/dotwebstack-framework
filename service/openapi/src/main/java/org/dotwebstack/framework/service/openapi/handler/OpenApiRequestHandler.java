package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.helper.VendorExtensionHelper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class OpenApiRequestHandler implements HandlerFunction<ServerResponse> {

  private final String filePath = "config/openapi.yaml";

  @Override
  public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
    try {
      InputStream openApiStream = this.getClass()
          .getClassLoader()
          .getResourceAsStream(filePath);
      YAMLMapper mapper = new YAMLMapper();
      ObjectNode specNode = VendorExtensionHelper.removeVendorExtensions(openApiStream, mapper);
      String openApiSpec = mapper.writer()
          .writeValueAsString(specNode);
      return ServerResponse.ok()
          .contentType(MediaType.parseMediaType("application/json"))
          .body(fromPublisher(Mono.just(openApiSpec), String.class));
    } catch (IOException e) {
      throw invalidConfigurationException("An unexpected error occurred while parsing the OpenApi Specification: {}",
          e);
    }
  }
}
