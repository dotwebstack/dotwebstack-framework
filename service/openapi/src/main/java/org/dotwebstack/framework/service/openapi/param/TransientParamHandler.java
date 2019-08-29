package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.hasDwsExtensionWithValue;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TRANSIENT;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class TransientParamHandler extends DefaultParamHandler {

  public TransientParamHandler(OpenAPI openApi) {
    super(openApi);
  }

  @Override
  public boolean supports(Parameter parameter) {
    return hasDwsExtensionWithValue(parameter, X_DWS_TRANSIENT, Boolean.TRUE);
  }

  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter,
      @NonNull ResponseSchemaContext responseSchemaContext) {
    return super.getValue(request, parameter, responseSchemaContext);
  }

  @Override
  public void validate(@NonNull GraphQlField graphQlField, @NonNull Parameter parameter, @NonNull String pathName) {
    return;
  }
}
