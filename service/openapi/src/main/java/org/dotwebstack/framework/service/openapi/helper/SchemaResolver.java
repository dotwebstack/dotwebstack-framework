package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class SchemaResolver {

  public static RequestBody resolveRequestBody(@NonNull OpenAPI openApi, RequestBody requestBody) {
    if (Objects.isNull(requestBody)) {
      return requestBody;
    }

    String ref = requestBody.get$ref();

    if (Objects.nonNull(ref)) {
      String refName = StringUtils.substringAfterLast(ref, "/");

      if (StringUtils.isBlank(refName)) {
        throw invalidOpenApiConfigurationException("RequestBody reference '{}' is invalid", ref);
      }

      RequestBody result = openApi.getComponents()
          .getRequestBodies()
          .get(refName);

      if (Objects.isNull(result)) {
        throw invalidOpenApiConfigurationException("RequestBody definition can't be found for reference '{}'", ref);
      }

      return result;
    }

    return requestBody;
  }

  public static Schema<?> resolveSchema(@NonNull OpenAPI openApi, @NonNull Schema<?> schema) {
    String ref = schema.get$ref();

    if (Objects.nonNull(ref)) {
      String refName = StringUtils.substringAfterLast(ref, "/");

      if (StringUtils.isBlank(refName)) {
        throw invalidOpenApiConfigurationException("Schema reference '{}' is invalid", ref);
      }

      Schema<?> result = openApi.getComponents()
          .getSchemas()
          .get(refName);

      if (Objects.isNull(result)) {
        throw invalidOpenApiConfigurationException("Schema definition can't be found for reference '{}'", ref);
      }

      return result;
    }

    return schema;
  }
}
