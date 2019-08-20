package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_SORT_TYPE;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class SortParamHandler extends DefaultParamHandler {

  public SortParamHandler(OpenAPI openApi) {
    super(openApi);
  }

  @Override
  public boolean supports(Parameter parameter) {
    return supportsDwsType(parameter, X_DWS_SORT_TYPE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter,
      @NonNull ResponseContext responseContext) {
    Optional<Object> value = super.getValue(request, parameter, responseContext);

    if (value.isPresent()) {
      String type = parameter.getSchema()
          .getType();
      switch (type) {
        case ARRAY_TYPE:
          List<String> list;
          if (value.get() instanceof String) {
            list = Stream.of(((String) value.get()).replace("[", "")
                .replace("]", ""))
                .collect(Collectors.toList());
          } else if (value.get() instanceof List) {
            list = new ArrayList<>(((List<String>) value.get()));
          } else {
            throw parameterValidationException("Sort parameter '{}' is of wrong type, can only be string or string[].",
                parameter.getName());
          }
          return Optional.of(list.stream()
              .map(this::parseSortParam)
              .collect(Collectors.toList()));
        case STRING_TYPE:
          return Optional.of(Collections.singletonList(this.parseSortParam((String) value.get())));
        default:
          throw parameterValidationException("Sort parameter '%s' is of wrong type, can only be string or string[].",
              parameter.getName());
      }
    }
    return value;
  }

  private Map<String, String> parseSortParam(String sortString) {
    String order = "ASC";
    String field = sortString.replace("\"", "");
    if (field.startsWith("-")) {
      order = "DESC";
      field = field.substring(1);
    }

    return ImmutableMap.of("field", field, "order", order);
  }
}
