package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.helper.SchemaUtils;

public class RequestBodyContextBuilder {

  private final OpenAPI openApi;

  public RequestBodyContextBuilder(OpenAPI openApi) {
    this.openApi = openApi;
  }

  public RequestBodyContext buildRequestBodyContext(RequestBody requestBody) {
    if (requestBody != null) {
      return new RequestBodyContext(getPropertyName(requestBody), requestBody);
    } else {
      return null;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private String getPropertyName(RequestBody requestBody) {
    io.swagger.v3.oas.models.media.MediaType mediaType = requestBody.getContent()
        .get("application/json");
    if (mediaType == null) {
      throw ExceptionHelper.invalidConfigurationException("Media type 'application/json' not found on request body.");
    }
    Schema schema = mediaType.getSchema();
    if (schema.get$ref() != null) {
      schema = SchemaUtils.getSchemaReference(schema.get$ref(), openApi);
    }
    List<String> propertyNames = new ArrayList<>(((Set<String>) schema.getProperties()
        .keySet()));
    if (propertyNames.size() != 1) {
      throw ExceptionHelper.invalidConfigurationException(
          "Request body schema should contain exactly 1 property, found properties ().", propertyNames.size());
    }
    return propertyNames.get(0);
  }
}
