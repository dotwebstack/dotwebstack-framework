package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class SortParamHandler extends DefaultParamHandler {

  @Override
  public boolean supports(Parameter parameter) {
    Map<String, Object> extensions = parameter.getExtensions();
    if (Objects.nonNull(extensions)) {
      String handler = (String) extensions.get("x-dws-type");
      if (Objects.nonNull(handler)) {
        return handler.equals("sort");
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<Object> getValue(ServerRequest request, Parameter parameter) throws ParameterValidationException {
    Optional<Object> value = super.getValue(request, parameter);

    if (value.isPresent()) {
      String type = parameter.getSchema()
          .getType();
      switch (type) {
        case "array":
          List<String> list;
          if (value.get() instanceof String) {
            list = Stream.of(((String) value.get()).replace("[", "")
                .replace("]", ""))
                .collect(Collectors.toList());
          } else if (value.get() instanceof List) {
            list = new ArrayList<>(((List<String>) value.get()));
          } else {
            throw new ParameterValidationException(String
                .format("Sort parameter '%s' is of wrong type, can only be string of string[]", parameter.getName()));
          }
          return Optional.of(list.stream()
              .map(this::parseSortParam)
              .collect(Collectors.joining(", ", "[", "]")));
        case "string":
          return Optional.of("[" + this.parseSortParam((String) value.get()) + "]");
        default:
          throw new ParameterValidationException(String
              .format("Sort parameter '%s' is of wrong type, can only be string of string[]", parameter.getName()));
      }
    }

    return value;
  }

  private String parseSortParam(String sortString) {
    String order = "ASC";
    String field = sortString.replace("\"", "");
    if (field.startsWith("-")) {
      order = "DESC";
      field = field.substring(1);
    }

    return "{field: \"" + field + "\", order: " + order + "}";
  }
}
