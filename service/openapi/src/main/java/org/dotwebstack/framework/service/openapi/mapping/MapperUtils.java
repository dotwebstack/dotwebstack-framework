package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
}
