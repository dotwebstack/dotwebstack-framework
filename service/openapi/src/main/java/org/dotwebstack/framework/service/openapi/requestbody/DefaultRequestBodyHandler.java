package org.dotwebstack.framework.service.openapi.requestbody;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.badRequestException;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.helper.JsonNodeUtils;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Component
public class DefaultRequestBodyHandler implements RequestBodyHandler {

  private final OpenAPI openApi;

  private final ObjectMapper objectMapper;

  public DefaultRequestBodyHandler(@NonNull OpenAPI openApi,
      @NonNull Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
    this.openApi = openApi;
    this.objectMapper = jackson2ObjectMapperBuilder.build();
  }

  @Override
  public Map<String, Object> getValues(@NonNull ServerRequest request, @NonNull RequestBodyContext requestBodyContext,
      @NonNull RequestBody requestBody, @NonNull Map<String, Object> parameterMap) throws BadRequestException {
    Mono<String> mono = request.bodyToMono(String.class);
    String value = mono.block();

    if (Objects.isNull(value) && Boolean.TRUE.equals(requestBody.getRequired())) {
      throw badRequestException("Request body required but not found.");
    } else if (Objects.isNull(value)) {
      return Collections.emptyMap();
    } else {
      validateContentType(request);
      try {
        JsonNode node = objectMapper.reader()
            .readTree(value);
        Map<String, Object> result = new HashMap<>();
        node.fields()
            .forEachRemaining(field -> result.put(field.getKey(), JsonNodeUtils.toObject(field.getValue())));
        return result;
      } catch (JsonProcessingException e) {
        throw badRequestException("Request body is invalid", e);
      }
    }
  }

  @Override
  public void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName) {
    requestBody.getContent()
        .forEach((key, mediaType) -> {
          Schema<?> schema = resolveSchema(openApi, mediaType.getSchema());
          String type = schema.getType();
          if (!Objects.equals(type, OasConstants.OBJECT_TYPE)) {
            throw invalidConfigurationException("Schema type '{}' not supported for request body.", type);
          }
        });
  }

  private void validateContentType(ServerRequest request) throws BadRequestException {
    List<String> contentTypeHeaders = request.headers()
        .header(OasConstants.HEADER_CONTENT_TYPE);
    if (contentTypeHeaders.size() != 1) {
      throw badRequestException("Expected exactly 1 '{}' header but found {}.", OasConstants.HEADER_CONTENT_TYPE,
          contentTypeHeaders.size());
    } else if (!MediaType.APPLICATION_JSON.equalsTypeAndSubtype(MediaType.parseMediaType(contentTypeHeaders.get(0)))) {
      throw new UnsupportedMediaTypeException(MediaType.parseMediaType(contentTypeHeaders.get(0)),
          singletonList(MediaType.APPLICATION_JSON));
    }
  }

  @Override
  public boolean supports(@NonNull RequestBody requestBody) {
    return Objects.nonNull(requestBody.getContent()
        .get(MediaType.APPLICATION_JSON.toString()));
  }
}
