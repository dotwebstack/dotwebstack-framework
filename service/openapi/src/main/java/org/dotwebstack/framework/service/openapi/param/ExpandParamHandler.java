package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class ExpandParamHandler extends DefaultParamHandler {

  public ExpandParamHandler(OpenAPI openApi) {
    super(openApi);
  }

  @Override
  public boolean supports(Parameter parameter) {
    Map<String, Object> extensions = parameter.getExtensions();
    if (Objects.nonNull(extensions)) {
      return Objects.nonNull(extensions.get("x-dws-expand"));
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter)
      throws ParameterValidationException {
    Optional<Object> expandValueOptional = super.getValue(request, parameter);

    if (expandValueOptional.isPresent()) {
      List<String> allValues = new ArrayList<>();
      ((List<String>) expandValueOptional.get()).forEach(value -> {
        String[] path = value.split("\\.");
        StringBuilder pathBuilder = new StringBuilder();
        Stream.of(path)
            .forEach(pathElement -> {
              if (!pathBuilder.toString()
                  .equals("")) {
                pathBuilder.append(".");
              }
              pathBuilder.append(pathElement);

              if (!allValues.contains(pathBuilder.toString())) {
                allValues.add(pathBuilder.toString());
              }
            });
      });
      Collections.sort(allValues);
      return Optional.of(allValues);
    }
    return expandValueOptional;
  }

  @Override
  public void validate(@NonNull GraphQlField graphQlField, @NonNull Parameter parameter, @NonNull String pathName) {

  }

  @Override
  public String getName(String name) {
    return "x-dws-expand";
  }
}
