package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.Field;
import graphql.language.SelectionSet;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class GeometryTypeMapper implements TypeMapper {

  private static final String TYPE_NAME = "Geometry";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};

  @Override
  public List<Field> schemaToField(@NonNull String name, @NonNull Schema<?> schema) {
    if (!"object".equals(schema.getType())) {
      throw invalidConfigurationException("Geometry type expects required an object field (found: {}).",
          schema.getName());
    }

    var field = new Field(name, new SelectionSet(List.of(new Field("asGeoJSON"))));

    return List.of(field);
  }

  @Override
  public Object fieldToBody(@NonNull Object data) {
    if (!(data instanceof Map)) {
      throw invalidConfigurationException("Geometry type expects a map result.");
    }

    @SuppressWarnings("unchecked")
    var geoJsonString = ((Map<String, String>) data).get("asGeoJSON");

    try {
      return OBJECT_MAPPER.readValue(geoJsonString, TYPE_REFERENCE);
    } catch (JsonProcessingException e) {
      throw internalServerErrorException("Error while parsing GeoJSON string.", e);
    }
  }

  @Override
  public String typeName() {
    return TYPE_NAME;
  }
}
