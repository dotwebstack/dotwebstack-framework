package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.badRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.IntValue;
import graphql.language.SelectionSet;
import io.swagger.v3.oas.models.media.Schema;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.springframework.stereotype.Component;

@Component
public class GeometryTypeMapper implements TypeMapper {

  private static final String OBJECT_TYPE = "object";

  private static final String ARRAY_TYPE = "array";

  private static final List<String> ALLOWED_TYPES = List.of(OBJECT_TYPE, ARRAY_TYPE);

  private static final String TYPE_NAME = "Geometry";

  private static final String AS_GEO_JSON_FIELD = "asGeoJSON";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};

  private final OpenApiProperties openApiProperties;

  public GeometryTypeMapper(@NonNull OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }


  @Override
  public List<Field> schemaToField(@NonNull String name, @NonNull Schema<?> schema,
      @NonNull Map<String, Object> parameters) {
    validateSchemaType(schema);

    var builder = Field.newField(name, new SelectionSet(List.of(new Field(AS_GEO_JSON_FIELD))));
    resolveSridArgument(parameters).ifPresent(argument -> builder.arguments(List.of(argument)));

    return List.of(builder.build());
  }

  @Override
  public Object fieldToBody(@NonNull Object data, @NonNull Schema<?> schema) {
    validateSchemaType(schema);

    if (ARRAY_TYPE.equals(schema.getType())) {
      return getGeoJsonAsArray(data);
    } else {
      return getGeoJsonAsObject(data);
    }
  }

  private Object getGeoJsonAsArray(Object obj) {
    var geoJsonMap = getGeoJsonMap(obj);

    return Optional.of(geoJsonMap)
        .filter(e -> e.containsKey("geometries"))
        .map(e -> e.get("geometries"))
        .orElseThrow(() -> invalidConfigurationException("No key named 'geometries' found in map result."));
  }

  private Object getGeoJsonAsObject(Object obj) {
    return getGeoJsonMap(obj);
  }

  private Map<String, Object> getGeoJsonMap(Object obj) {
    var data = getGeoJson(obj);

    try {
      return OBJECT_MAPPER.readValue(data, TYPE_REFERENCE);
    } catch (JsonProcessingException e) {
      throw illegalArgumentException("Error while parsing GeoJSON string.", e);
    }
  }

  private String getGeoJson(Object obj) {
    var data = Optional.ofNullable(obj)
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .orElseThrow(() -> invalidConfigurationException("Geometry type expects a map result."));

    return Optional.of(data)
        .filter(e -> e.containsKey(AS_GEO_JSON_FIELD))
        .map(e -> e.get(AS_GEO_JSON_FIELD))
        .map(String.class::cast)
        .orElseThrow(() -> invalidConfigurationException("No key named `asGeoJSON` found in map result."));
  }

  private void validateSchemaType(Schema<?> schema) {
    if (!ALLOWED_TYPES.contains(schema.getType())) {
      throw invalidConfigurationException("Geometry type requires an object or array schema type (found: {}).",
          schema.getType());
    }
  }

  @Override
  public String typeName() {
    return TYPE_NAME;
  }

  private Optional<Argument> resolveSridArgument(Map<String, Object> parameters) {
    var spatial = this.openApiProperties.getSpatial();
    if (spatial == null) {
      return Optional.empty();
    } else {
      var sridParam = spatial.getSridParameter();
      var name = sridParam.getName();
      Object value = parameters.get(name);
      if (value == null) {
        return Optional.empty();
      }
      if (sridParam.getValueMap() == null) {
        return resolveSridFromValue(name, value);
      } else {
        return resolveSridFromValueMap(name, value, sridParam);
      }
    }
  }

  private Optional<Argument> resolveSridFromValue(String name, Object value) {
    BigInteger intValue;
    if (value instanceof String string) {
      try {
        intValue = new BigInteger(string);
      } catch (NumberFormatException e) {
        throw badRequestException(
            "Invalid srid parameter '{}' String value '{}', value could not be converted to an Integer.", name, value,
            e);
      }
    } else if (value instanceof Integer integer) {
      intValue = BigInteger.valueOf(integer);
    } else {
      throw badRequestException("Unsupported srid parameter '{}' type '{}', supported types are 'String', 'Integer'.",
          name, value.getClass(), value);
    }
    return Optional.of(Argument.newArgument("srid", new IntValue(intValue))
        .build());
  }

  private Optional<Argument> resolveSridFromValueMap(String name, Object value,
      OpenApiProperties.SridParameterProperties sridParam) {
    if (!(value instanceof String stringValue)) {
      throw badRequestException("Unsupported srid parameter '{}' type '{}', supported type is 'String'.", name,
          value.getClass());
    }

    if (!sridParam.getValueMap()
        .containsKey(stringValue)) {
      throw badRequestException("Unsupported srid parameter '{}' value '{}', supported values are {}.", name, value,
          sridParam.getValueMap()
              .keySet());
    }

    return Optional.of(Argument.newArgument("srid", new IntValue(BigInteger.valueOf(sridParam.getValueMap()
        .get(stringValue))))
        .build());
  }

}
