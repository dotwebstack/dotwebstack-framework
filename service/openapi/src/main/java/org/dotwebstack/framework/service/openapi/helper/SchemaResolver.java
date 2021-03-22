package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public class SchemaResolver {

  private SchemaResolver() {}

  public static Schema<?> resolveSchema(@NonNull OpenAPI openApi, @NonNull Schema<?> schema) {
    return resolveSchema(openApi, schema, schema.get$ref());
  }

  public static Schema<?> resolveSchema(@NonNull OpenAPI openApi, Schema<?> schema, String ref) {
    if (Objects.nonNull(ref)) {
      return resolveSchema(openApi, ref);
    }

    if (Objects.isNull(schema)) {
      throw invalidOpenApiConfigurationException("Schema can't be null if ref is also null.");
    }

    return schema;
  }

  @SuppressWarnings("rawtypes")
  public static Schema resolveSchema(@NonNull OpenAPI openApi, @NonNull String ref) {
    String[] path = StringUtils.substringAfter(ref, "components/")
        .split("/");
    if (path.length != 2) {
      throw invalidOpenApiConfigurationException(
          "Schema reference '{}' should start with #/components/headers or #/components/schemas", ref);
    }

    Schema<?> result;
    switch (path[0]) {
      case "schemas":
        result = openApi.getComponents()
            .getSchemas()
            .get(path[1]);
        break;
      case "headers":
        result = openApi.getComponents()
            .getHeaders()
            .get(path[1])
            .getSchema();
        break;
      default:
        throw invalidOpenApiConfigurationException(
            "Schema reference '{}' should start with #/components/headers or #/components/schema", ref);
    }

    if (Objects.isNull(result)) {
      throw invalidOpenApiConfigurationException("Schema definition can't be found for reference '{}'", ref);
    }

    return result;
  }
}
