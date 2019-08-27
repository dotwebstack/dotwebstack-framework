package org.dotwebstack.framework.service.openapi.helper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

public class SchemaResolver {

  public static RequestBody resolveRequestBody(@NonNull OpenAPI openAPI, RequestBody requestBody) {
    if(Objects.isNull(requestBody)) {
      return requestBody;
    }

    String ref = requestBody.get$ref();

    if (Objects.nonNull(ref)) {
      String refName = StringUtils.substringAfterLast(ref,"/");

      if(StringUtils.isBlank(refName)) {
        throw invalidOpenApiConfigurationException("RequestBody reference '{}' is invalid",ref);
      }

      RequestBody result = openAPI.getComponents().getRequestBodies().get(refName);

      if(Objects.isNull(result)){
        throw invalidOpenApiConfigurationException("RequestBody definition can't be found for reference '{}'",ref);
      }

      return result;
    }

    return requestBody;
  }

  public static Schema resolveSchema(@NonNull OpenAPI openAPI, @NonNull Schema schema) {
    String ref = schema.get$ref();

    if (Objects.nonNull(ref)) {
      String refName = StringUtils.substringAfterLast(ref,"/");

      if(StringUtils.isBlank(refName)) {
        throw invalidOpenApiConfigurationException("Schema reference '{}' is invalid",ref);
      }

      Schema result = openAPI.getComponents().getSchemas().get(ref);

      if(Objects.isNull(result)){
        throw invalidOpenApiConfigurationException("Schema definition can't be found for reference '{}'",ref);
      }

      return result;
    }

    return schema;
  }
}
