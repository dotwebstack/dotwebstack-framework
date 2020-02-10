package org.dotwebstack.framework.service.openapi.helper;

import static java.lang.String.format;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class RequestBodyResolver {

  public static final String REQUEST_BODIES_PATH = "#/components/requestBodies/";

  private RequestBodyResolver() {
    // hide constructor
  }

  public static RequestBody resolveRequestBody(@NonNull OpenAPI openApi, @NonNull RequestBody requestBody) {
    if (Objects.nonNull(requestBody.get$ref())) {
      String ref = requestBody.get$ref();
      if (!ref.startsWith(REQUEST_BODIES_PATH)) {
        throw ExceptionHelper.invalidConfigurationException(
            format("$ref [%s] for requestBody should start with [%s]", ref, REQUEST_BODIES_PATH));
      }
      String requestBodyName = ref.substring(REQUEST_BODIES_PATH.length());
      Map<String, RequestBody> requestBodies = openApi.getComponents()
          .getRequestBodies();
      if (Objects.isNull(requestBodies) || Objects.isNull(requestBodies.get(requestBodyName))) {
        throw ExceptionHelper.invalidConfigurationException(
            format("Could not find requestBody with name [%s] from $ref [%s]", requestBodyName, ref));
      }
      return requestBodies.get(requestBodyName);
    } else {
      return requestBody;
    }
  }
}
