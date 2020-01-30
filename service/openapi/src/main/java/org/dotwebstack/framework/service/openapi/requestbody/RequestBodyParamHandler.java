package org.dotwebstack.framework.service.openapi.requestbody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;
import java.util.Objects;

import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.supportsDwsType;

public class RequestBodyParamHandler implements RequestBodyHandler {

  private static final String INPUT_PARAMS = "inputParams";

  private OpenAPI openApi;

  @Override
  public Map<String, Object> getValues(@NonNull ServerRequest request, @NonNull RequestBody requestBody, Map<String, Object> parameterMap) throws BadRequestException {
    return null;
  }

  @Override
  public void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName) {

  }

  @Override
  public boolean supports(@NonNull RequestBody requestBody) {
    String ref = requestBody.get$ref();

    if (Objects.nonNull(ref)) {
      RequestBody schema = openApi.getComponents()
          .getRequestBodies()
          .get(StringUtils.substringAfterLast(ref, "/"));
      return supportsDwsType(schema, INPUT_PARAMS);
    }

    return supportsDwsType(requestBody, INPUT_PARAMS);
  }
}
