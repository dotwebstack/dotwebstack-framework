package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Objects;
import lombok.NonNull;

public class SchemaUtils {

  private SchemaUtils() {}

  @SuppressWarnings("rawtypes")
  public static Schema getSchemaReference(@NonNull String ref, @NonNull OpenAPI openApi) {
    String[] refPath = ref.split("/");
    Schema result = openApi.getComponents()
        .getSchemas()
        .get(refPath[refPath.length - 1]);

    if (Objects.isNull(result)) {
      throw invalidConfigurationException("Schema '{}' not found in configuration.", ref);
    }

    return result;
  }
}
