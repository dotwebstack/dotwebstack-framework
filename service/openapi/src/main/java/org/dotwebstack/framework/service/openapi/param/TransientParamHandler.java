package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.hasDwsExtensionWithValue;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TRANSIENT;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;

@Component
public class TransientParamHandler extends DefaultParamHandler {

  public TransientParamHandler(OpenAPI openApi) {
    super(openApi);
  }

  @Override
  public boolean supports(Parameter parameter) {
    return hasDwsExtensionWithValue(parameter, X_DWS_TRANSIENT, Boolean.TRUE);
  }
}
