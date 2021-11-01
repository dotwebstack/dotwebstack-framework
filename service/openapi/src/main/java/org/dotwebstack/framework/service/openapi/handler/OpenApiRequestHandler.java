package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.helper.VendorExtensionHelper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class OpenApiRequestHandler implements HandlerFunction<ServerResponse> {

  private final String openApiSpec;

  public OpenApiRequestHandler(InputStream openApiStream) {
    try {
      var mapper = new YAMLMapper();
      ObjectNode specNode = VendorExtensionHelper.removeVendorExtensions(openApiStream, mapper);
      openApiSpec = mapper.writer()
          .writeValueAsString(specNode);
    } catch (IOException e) {
      throw invalidConfigurationException("An unexpected error occurred while parsing the OpenApi Specification: {}",
          e);
    }
  }

  @Override
  public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.parseMediaType("text/yaml; charset=utf-8"))
        .body(BodyInserters.fromValue(openApiSpec));
  }
}
