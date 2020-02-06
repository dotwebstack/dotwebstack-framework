package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Objects;
import org.dotwebstack.framework.service.openapi.helper.RequestBodyResolver;

public class RequestBodyContextBuilder {

  private final OpenAPI openApi;

  public RequestBodyContextBuilder(OpenAPI openApi) {
    this.openApi = openApi;
  }

  public RequestBodyContext buildRequestBodyContext(RequestBody requestBody) {
    if (requestBody != null) {
      RequestBody resolvedRequestBody = RequestBodyResolver.resolveRequestBody(openApi, requestBody);
      validate(resolvedRequestBody);
      return new RequestBodyContext(resolvedRequestBody);
    } else {
      return null;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void validate(RequestBody requestBody) {
    if (Objects.isNull(requestBody.getContent())) {
      throw illegalArgumentException("RequestBody without content!");
    }

    MediaType mediaType = requestBody.getContent()
        .get("application/json");;

    if (Objects.isNull(mediaType)) {
      throw invalidConfigurationException("Media type 'application/json' not found on request body.");
    }
  }
}
