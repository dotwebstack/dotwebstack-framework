package org.dotwebstack.framework.service.openapi.param;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface ParamHandler {

  boolean supports(@NonNull Parameter parameter);

  Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter,
      @NonNull ResponseSchemaContext responseSchemaContext);

  void validate(@NonNull GraphQlField graphQlField, @NonNull Parameter parameter, @NonNull String pathName);

  default String getParameterName(String name) {
    return name;
  }
}
