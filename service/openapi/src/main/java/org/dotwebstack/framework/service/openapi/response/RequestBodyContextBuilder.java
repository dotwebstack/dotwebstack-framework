package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveRequestBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dotwebstack.framework.service.openapi.helper.SchemaUtils;

public class RequestBodyContextBuilder {

  private final OpenAPI openApi;

  public RequestBodyContextBuilder(OpenAPI openApi) {
    this.openApi = openApi;
  }

  public RequestBodyContext buildRequestBodyContext(RequestBody requestBody) {
    if (requestBody != null) {
      return new RequestBodyContext(getPropertyName(resolveRequestBody(openApi, requestBody)), requestBody);
    } else {
      return null;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private String getPropertyName(RequestBody requestBody) {
    Content content = requestBody.getContent();

    if (Objects.isNull(content)) {
      throw illegalArgumentException("RequestBody without content!");
    }

    MediaType mediaType = content.get("application/json");;

    if (Objects.isNull(mediaType)) {
      throw invalidConfigurationException("Media type 'application/json' not found on request body.");
    }
    Schema schema = mediaType.getSchema();
    if (schema.get$ref() != null) {
      schema = SchemaUtils.getSchemaReference(schema.get$ref(), openApi);
    }
    List<String> propertyNames = new ArrayList<>(((Set<String>) schema.getProperties()
        .keySet()));
    if (propertyNames.size() != 1) {
      throw invalidConfigurationException("Request body schema should contain exactly 1 property, found properties ().",
          propertyNames.size());
    }
    return propertyNames.get(0);
  }
}
