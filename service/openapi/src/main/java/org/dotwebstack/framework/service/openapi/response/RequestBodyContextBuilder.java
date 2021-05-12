package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
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
      var resolvedRequestBody = RequestBodyResolver.resolveRequestBody(openApi, requestBody);
      validate(resolvedRequestBody);
      return new RequestBodyContext(resolvedRequestBody);
    } else {
      return null;
    }
  }

  static void validate(RequestBody requestBody) {
    if (Objects.isNull(requestBody.getContent())) {
      throw illegalArgumentException("RequestBody without content!");
    }

    var mediaType = requestBody.getContent()
        .get(org.springframework.http.MediaType.APPLICATION_JSON.toString());

    if (Objects.isNull(mediaType)) {
      throw invalidConfigurationException("Media type '{}' not found on request body.",
          org.springframework.http.MediaType.APPLICATION_JSON.toString());
    }
  }
}
