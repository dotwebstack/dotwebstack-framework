package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class MapperUtils {

  private MapperUtils() {}

  public static <T> Collector<T, ?, Optional<T>> collectExactlyOne() {
    return Collectors.collectingAndThen(Collectors.toList(),
        list -> list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty());
  }

  public static ApiResponse getSuccessResponse(Operation operation) {
    return operation.getResponses()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey()
            .matches("^2\\d{2}$"))
        .map(Map.Entry::getValue)
        .collect(MapperUtils.collectExactlyOne())
        .orElseThrow(
            () -> invalidOpenApiConfigurationException("Operation does not contain exactly one success response."));
  }

  public static boolean isEnvelope(Schema<?> schema) {
    return Optional.ofNullable(schema.getExtensions())
        .map(extensions -> Boolean.TRUE.equals(extensions.get(OasConstants.X_DWS_ENVELOPE)))
        .orElse(false);
  }

  public static GraphQLFieldDefinition getObjectField(GraphQLObjectType objectType, String fieldName) {
    return Optional.ofNullable(objectType.getFieldDefinition(fieldName))
        .orElseThrow(() -> invalidConfigurationException("Field '{}' not found for `{}` type.", fieldName,
            objectType.getName()));
  }
}
