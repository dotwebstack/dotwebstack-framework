package org.dotwebstack.framework.service.openapi.param;

import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.PIPEDELIMITED;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.SIMPLE;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.SPACEDELIMITED;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.DATETIME_FORMAT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.DATE_FORMAT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.INTEGER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.NUMBER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_HEADER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_PATH_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_QUERY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_VALIDATE;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;
import static org.dotwebstack.framework.service.openapi.param.ParamValueCaster.cast;
import static org.dotwebstack.framework.service.openapi.param.ParamValueCaster.castArray;
import static org.dotwebstack.framework.service.openapi.param.ParamValueCaster.castList;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.JsonNodeUtils;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.springframework.web.reactive.function.server.ServerRequest;

public class DefaultParamHandler implements ParamHandler {

  protected final OpenAPI openApi;

  public DefaultParamHandler(OpenAPI openApi) {
    this.openApi = openApi;
  }

  @Override
  public boolean supports(Parameter parameter) {
    return true;
  }

  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter) {
    Object paramValue;
    switch (parameter.getIn()) {
      case PARAM_PATH_TYPE:
        paramValue = getPathParam(parameter, request);
        break;
      case PARAM_QUERY_TYPE:
        paramValue = getQueryParam(parameter, request);
        break;
      case PARAM_HEADER_TYPE:
        paramValue = getHeaderParam(parameter, request);
        break;
      default:
        throw illegalArgumentException("Unsupported value for parameters.in: '{}'.", parameter.getIn());
    }

    if (Objects.nonNull(paramValue)) {
      Object convertedValue = deserialize(parameter, paramValue);
      validateValues(convertedValue, parameter);
      return Optional.of(convertedValue);
    } else {
      Optional<Object> defaultValue = getDefault(parameter);
      if (defaultValue.isPresent()) {
        validateValues(defaultValue.get(), parameter);
      } else {
        if (Boolean.TRUE.equals(parameter.getRequired())) {
          if (Objects.nonNull(parameter.getExtensions()) && parameter.getExtensions()
              .containsKey(X_DWS_VALIDATE)) {
            return Optional.empty();
          }
          throw parameterValidationException("No value provided for required {} parameter '{}'.", parameter.getIn(),
              parameter.getName());
        }
      }

      return defaultValue;
    }
  }

  @Override
  public void validate(GraphQlField field, Parameter parameter, String pathName) {
    String parameterName = parameter.getName();
    if (Objects.nonNull(parameter.getExtensions())) {
      if (parameter.getExtensions()
          .containsKey(X_DWS_VALIDATE)) {
        return;
      }
      if (parameter.getExtensions()
          .containsKey(X_DWS_NAME)) {
        parameterName = (String) parameter.getExtensions()
            .get(X_DWS_NAME);
      }
    }

    this.validate(field, parameterName, pathName);
  }

  public void validate(GraphQlField field, String parameterName, String pathName) {
    if (field.getArguments()
        .stream()
        .noneMatch(argument -> argument.getName()
            .equals(parameterName))) {
      throw ExceptionHelper.invalidConfigurationException(
          "OAS argument '{}' for path '{}' was not found on GraphQL field '{}'", parameterName, pathName,
          field.getName());
    }
  }

  void validateValues(Object paramValue, Parameter parameter) {
    switch (parameter.getSchema()
        .getType()) {
      case ARRAY_TYPE:
        validateEnumValuesForArray(paramValue, parameter);
        break;
      case STRING_TYPE:
        validateEnum(paramValue, parameter);
        validatePattern(paramValue.toString(), parameter);
        validateDate(paramValue, parameter);
        validateDateTime(paramValue, parameter);
        break;
      case INTEGER_TYPE:
        validateInteger(paramValue, parameter);
        break;
      case NUMBER_TYPE:
        validateNumber(paramValue, parameter);
        break;
      default:
        if (hasEnum(parameter)) {
          throw parameterValidationException("Sort parameter '{}' is of wrong type, can only be string or string[]",
              parameter.getName());
        }
    }
  }

  @SuppressWarnings("unchecked")
  private void validateEnum(Object paramValue, Parameter parameter) {
    if (hasEnum(parameter) && !parameter.getSchema()
        .getEnum()
        .contains(paramValue)) {
      throw parameterValidationException("Parameter '{}' has (an) invalid value(s): '{}', should be one of: '{}'",
          parameter.getName(), paramValue, String.join(", ", parameter.getSchema()
              .getEnum()));
    }
  }

  private void validatePattern(String paramValue, Parameter parameter) {
    if (parameter.getSchema() != null && parameter.getSchema()
        .getPattern() != null) {
      String pattern = parameter.getSchema()
          .getPattern();
      if (!paramValue.matches(pattern)) {
        throw parameterValidationException("Parameter '{}' with value '{}' does not match expected pattern '{}'",
            parameter.getName(), paramValue, String.join(", ", pattern));
      }
    }
  }

  private void validateDate(Object paramValue, Parameter parameter) {
    if (parameter.getSchema() != null && DATE_FORMAT.equals(parameter.getSchema()
        .getFormat())) {
      try {
        LocalDate.parse((String) paramValue);
      } catch (ClassCastException | DateTimeParseException e) {
        throw parameterValidationException("Parameter '{}' has an invalid value: '{}' for type: '{}' and format: '{}'",
            parameter.getName(), paramValue, parameter.getSchema()
                .getType(),
            parameter.getSchema()
                .getFormat());
      }
    }
  }

  private void validateDateTime(Object paramValue, Parameter parameter) {
    if (parameter.getSchema() != null && DATETIME_FORMAT.equals(parameter.getSchema()
        .getFormat())) {
      try {
        ZonedDateTime.parse((String) paramValue);
      } catch (ClassCastException | DateTimeParseException e) {
        throw parameterValidationException("Parameter '{}' has an invalid value: '{}' for type: '{}' and format: '{}'",
            parameter.getName(), paramValue, parameter.getSchema()
                .getType(),
            parameter.getSchema()
                .getFormat());
      }
    }
  }

  private void validateInteger(Object paramValue, Parameter parameter) {
    try {
      if (!(paramValue instanceof Integer)) {
        Long.valueOf((String) paramValue);
      }
    } catch (ClassCastException | NumberFormatException exception) {
      throw parameterValidationException("Parameter '{}' has an invalid value: '{}' for type: '{}'",
          parameter.getName(), paramValue, parameter.getSchema()
              .getType());
    }
  }

  private void validateNumber(Object paramValue, Parameter parameter) {
    try {
      if (!(paramValue instanceof BigDecimal)) {
        new BigDecimal((String) paramValue);
      }
    } catch (ClassCastException | NumberFormatException exception) {
      throw parameterValidationException("Parameter '{}' has an invalid value: '{}' for type: '{}'",
          parameter.getName(), paramValue, parameter.getSchema()
              .getType());
    }
  }

  @SuppressWarnings("unchecked")
  private void validateEnumValuesForArray(Object paramValue, Parameter parameter) {
    if (hasEnum(parameter)) {
      List<String> list;
      List<String> enumList = (List<String>) ((ArraySchema) parameter.getSchema()).getItems()
          .getEnum();
      if (paramValue instanceof String) {
        list = Stream.of(((String) paramValue).replace("[", "")
            .replace("]", ""))
            .collect(Collectors.toList());
      } else if (paramValue instanceof List) {
        list = (List<String>) paramValue;
      } else {
        throw parameterValidationException("Enumerated parameter '{}' can only be of string or string[]",
            parameter.getName());
      }
      List<String> invalidValues = list.stream()
          .filter(param -> !enumList.contains(param))
          .collect(Collectors.toList());

      if (!invalidValues.isEmpty()) {
        throw parameterValidationException("Parameter '{}' has (an) invalid value(s): '{}', should be one of: '{}'",
            parameter.getName(), String.join(", ", invalidValues), String.join(", ", enumList));
      }
    }
  }

  private Object deserialize(Parameter parameter, Object paramValue) {
    if (paramValue == null) {
      return null;
    }
    Schema<?> schema = SchemaResolver.resolveSchema(openApi, parameter.getSchema(), parameter.get$ref());
    String schemaType = schema.getType();
    switch (schemaType) {
      case ARRAY_TYPE:
        return deserializeArray(parameter, (ArraySchema) schema, paramValue);
      case OBJECT_TYPE:
        return deserializeObject(parameter, schema, paramValue);
      default:
        return cast((String) paramValue, schema);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object deserializeArray(Parameter parameter, ArraySchema schema, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();
    Schema<?> itemSchema = resolveSchema(openApi, schema.getItems());

    if (style == SIMPLE && !explode) {
      return castArray(((String) paramValue).split(","), itemSchema);
    } else if (style == FORM && !explode) {
      return castArray(((String) paramValue).split(","), itemSchema);
    } else if (style == FORM) {
      return castList((List) paramValue, itemSchema);
    } else if (style == SPACEDELIMITED && !explode) {
      return castArray(((String) paramValue).split(" "), itemSchema);
    } else if (style == PIPEDELIMITED && !explode) {
      return castArray(((String) paramValue).split("\\|"), itemSchema);
    } else {
      throw ExceptionHelper.unsupportedOperationException(
          "Array deserialization not supported for parameter with 'explode=false' and style "
              + "'{}'. Supported styles are '{}'.",
          style, List.of(SIMPLE, FORM, SPACEDELIMITED, PIPEDELIMITED));
    }
  }

  private Object deserializeObject(Parameter parameter, Schema<?> schema, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == SIMPLE) {
      if (explode) {
        return deserializeObjectFromKeyValueString((String) paramValue, ",", "=", schema);
      }
      return deserializeObjectFromKeyValueString((String) paramValue, schema);
    } else {
      throw ExceptionHelper.unsupportedOperationException(
          "Object deserialization not supported for parameter style " + "'{}'. Supported styles are '{}'.", style,
          List.of(SIMPLE));
    }
  }

  private Object deserializeObjectFromKeyValueString(String keyValueString, Schema<?> schema) {
    String[] split = keyValueString.split(",");
    if (split.length % 2 != 0) {
      throw illegalArgumentException("Key value string '{}' should contain an even number of elements.",
          keyValueString);
    }
    Map<String, Object> result = new HashMap<>();
    for (var i = 0; i < split.length; i += 2) {
      String key = split[i];
      Schema<?> propertySchema = resolvePropertySchema(schema, key);
      Object value = cast(split[i + 1], propertySchema);
      result.put(key, value);
    }
    return result;
  }

  private Object deserializeObjectFromKeyValueString(String keyValueString, String elementSeparator,
      String keyValueSeparator, Schema<?> schema) {
    Map<String, Object> result = new HashMap<>();
    Arrays.asList(keyValueString.split(elementSeparator))
        .forEach(keyValue -> {
          String[] split = keyValue.split(keyValueSeparator);
          if (split.length != 2) {
            throw illegalArgumentException(
                "Key value element '{}' with separator '{}' should have one " + "key and one value.", keyValue,
                keyValueSeparator);
          }
          Schema<?> propertySchema = resolvePropertySchema(schema, split[0]);
          result.put(split[0], cast(split[1], propertySchema));
        });
    return result;
  }

  private Object getPathParam(Parameter parameter, ServerRequest request) {
    try {
      return request.pathVariable(parameter.getName());
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private Object getQueryParam(Parameter parameter, ServerRequest request) {
    List<String> result = request.queryParams()
        .get(parameter.getName());

    if (ARRAY_TYPE.equals(parameter.getSchema()
        .getType()) && Boolean.TRUE.equals(parameter.getExplode())) {
      return result;
    }
    return (!Objects.isNull(result) && !result.isEmpty()) ? result.get(0) : null;
  }

  private String getHeaderParam(Parameter parameter, ServerRequest request) {
    List<String> result = request.headers()
        .header(parameter.getName());
    if (!result.isEmpty()) {
      return String.join(",", result);
    }
    return null;
  }

  Optional<Object> getDefault(Parameter parameter) {
    var schema = resolveSchema(openApi, parameter.getSchema());
    if (schema != null && schema.getDefault() != null) {
      switch (schema.getType()) {
        case ARRAY_TYPE:
        case OBJECT_TYPE:
          return Optional.ofNullable(JsonNodeUtils.toObject((JsonNode) schema.getDefault()));
        default:
          return Optional.of(schema.getDefault());
      }
    }
    return Optional.empty();
  }

  private boolean hasEnum(Parameter parameter) {
    if (parameter.getSchema() instanceof ArraySchema) {
      var arraySchema = (ArraySchema) parameter.getSchema();
      return Objects.nonNull(arraySchema.getItems()
          .getEnum())
          && !arraySchema.getItems()
              .getEnum()
              .isEmpty();
    } else if (parameter.getSchema() instanceof StringSchema) {
      return Objects.nonNull(parameter.getSchema()
          .getEnum())
          && !parameter.getSchema()
              .getEnum()
              .isEmpty();
    }
    return false;
  }

  private Schema<?> resolvePropertySchema(Schema<?> schema, String key) {
    Schema<?> propertySchema = schema.getProperties()
        .get(key);
    if (propertySchema == null) {
      throw illegalArgumentException("Property {} was not found in schema", key);
    }
    return propertySchema;
  }

}
