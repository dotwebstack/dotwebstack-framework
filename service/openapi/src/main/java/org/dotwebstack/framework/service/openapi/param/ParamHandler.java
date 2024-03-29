package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_NAME;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface ParamHandler {

  boolean supports(@NonNull Parameter parameter);

  Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter);

  void validate(@NonNull GraphQlField graphQlField, @NonNull Parameter parameter, @NonNull String pathName);

  default String getParameterName(Parameter param) {
    if (Objects.nonNull(param.getExtensions())) {
      String dwsName = (String) param.getExtensions()
          .get(X_DWS_NAME);
      if (Objects.nonNull(dwsName)) {
        return dwsName;
      }
    }
    return param.getName();
  }

}
