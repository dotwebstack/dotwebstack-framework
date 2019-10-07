package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public class CoreRequestHelper {

  private CoreRequestHelper() {}

  public static Set<String> getParameterNamesOfType(List<Parameter> params, String type) {
    return params.stream()
        .filter(parameter -> Objects.equals(parameter.getIn(), type))
        .map(Parameter::getName)
        .collect(Collectors.toSet());
  }

  public static void validateParameterExistence(String type, Set<String> schemaParams, Set<String> givenParams) {
    List<String> nonExistentVariables = givenParams.stream()
        .filter(parameter -> !schemaParams.contains(parameter))
        .collect(Collectors.toList());

    if (!nonExistentVariables.isEmpty()) {
      throw invalidConfigurationException("The following request {} parameters are not allowed on this endpoint: {}",
          type, nonExistentVariables);
    }
  }

  public static void validateRequestBodyNonexistent(ServerRequest request) {
    Mono<?> mono = request.bodyToMono(String.class);
    Object value = mono.block();
    if (Objects.nonNull(value)) {
      throw invalidConfigurationException("A request body is not allowed for this request");
    }
  }

}
