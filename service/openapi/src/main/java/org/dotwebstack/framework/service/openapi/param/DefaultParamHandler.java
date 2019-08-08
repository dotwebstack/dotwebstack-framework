package org.dotwebstack.framework.service.openapi.param;

import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.PIPEDELIMITED;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.SIMPLE;
import static io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.SPACEDELIMITED;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.springframework.web.reactive.function.server.ServerRequest;

public class DefaultParamHandler implements ParamHandler {
  @Override
  public boolean supports(Parameter parameter) {
    return true;
  }

  @Override
  public Optional<Object> getValue(ServerRequest request, Parameter parameter) throws ParameterValidationException {
    Object paramValue;
    switch (parameter.getIn()) {
      case "path":
        paramValue = getPathParam(parameter, request);
        break;
      case "query":
        paramValue = getQueryParam(parameter, request);
        break;
      case "header":
        paramValue = getHeaderParam(parameter, request);
        break;
      default:
        throw ExceptionHelper.illegalArgumentException("Unsupported value for parameters.in: '{}'.", parameter.getIn());
    }

    if (Objects.nonNull(paramValue)) {
      Object convertedValue = deserialize(parameter, paramValue);
      validateEnumValues(convertedValue, parameter);
      return Optional.of(convertedValue);
    }

    return Optional.empty();
  }

  @Override
  public void validate(GraphQlField field, Parameter parameter, String pathName) {
    String name = parameter.getName();
    long matching = field.getArguments()
        .stream()
        .filter(argument -> argument.getName()
            .equals(name))
        .count();
    if (matching == 0) {
      throw ExceptionHelper.invalidConfigurationException(
          "OAS argument '{}' for path '{}' was " + "not " + "found on GraphQL field '{}'", name, pathName,
          field.getName());
    }
  }

  private void validateEnumValues(Object paramValue, Parameter parameter) throws ParameterValidationException {
    String type = parameter.getSchema()
        .getType();
    switch (type) {
      case "array":
        validateEnumValuesForArray(paramValue, parameter);
        break;
      case "string":
        if (Objects.nonNull(parameter.getSchema()
            .getEnum())
            && !parameter.getSchema()
                .getEnum()
                .contains(paramValue)) {
          throw new ParameterValidationException(
              String.format("Parameter '%s' has an invalid value, should be one of: '%s'", parameter.getName(),
                  String.join(",", ((StringSchema) parameter.getSchema()).getEnum())));
        }
        break;
      default:
        if (Objects.nonNull(parameter.getSchema()
            .getEnum())) {
          throw new ParameterValidationException(String
              .format("Sort parameter '%s' is of wrong type, can only be string of string[]", parameter.getName()));
        }
    }
  }

  @SuppressWarnings("unchecked")
  private void validateEnumValuesForArray(Object paramValue, Parameter parameter) throws ParameterValidationException {
    if (Objects.nonNull(((ArraySchema) parameter.getSchema()).getItems()
        .getEnum())) {
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
        throw new ParameterValidationException(
            String.format("Enumerated parameter '%s' can only be of string or string[]", parameter.getName()));
      }
      List<String> invalidValues = list.stream()
          .filter(param -> !enumList.contains(param))
          .collect(Collectors.toList());

      if (!invalidValues.isEmpty()) {
        throw new ParameterValidationException(
            String.format("Parameter '%s' has (an) invalid value(s): '%s', should be one of: '%s'", parameter.getName(),
                String.join(", ", invalidValues), String.join(", ", enumList)));
      }
    }
  }

  protected Object deserialize(Parameter parameter, Object paramValue) {
    if (paramValue == null) {
      return null;
    }
    String schemaType = parameter.getSchema()
        .get$ref() != null ? "object"
            : parameter.getSchema()
                .getType();
    switch (schemaType) {
      case "array":
        return deserializeArray(parameter, paramValue);
      case "object":
        return deserializeObject(parameter, paramValue);
      default:
        return paramValue;
    }
  }

  protected Object deserializeArray(Parameter parameter, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == SIMPLE && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == FORM && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == SPACEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(" "));
    } else if (style == PIPEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split("\\|"));
    } else {
      throw ExceptionHelper.unsupportedOperationException(
          "Array deserialization not supported for parameter with 'explode=false' and style "
              + "'{}'. Supported styles are '{}'.",
          style, ImmutableList.of(SIMPLE, FORM, SPACEDELIMITED, PIPEDELIMITED));
    }
  }

  protected Object deserializeObject(Parameter parameter, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == SIMPLE && !explode) {
      return deserializeObjectFromKeyValueString((String) paramValue, ",");
    } else if (style == SIMPLE && explode) {
      return deserializeObjectFromKeyValueString((String) paramValue, ",", "=");
    } else {
      throw ExceptionHelper.unsupportedOperationException(
          "Object deserialization not supported for parameter style " + "'{}'. Supported styles are '{}'.", style,
          ImmutableList.of(SIMPLE));
    }
  }

  protected Object deserializeObjectFromKeyValueString(String keyValueString, String separator) {
    String[] split = keyValueString.split(",");
    if (split.length % 2 != 0) {
      throw ExceptionHelper.illegalArgumentException(
          "Key value string '{}' with separator '{}' should contain an " + "even number of elements.", keyValueString,
          separator);
    }
    Map<String, String> result = new HashMap<>();
    for (int i = 0; i < split.length; i += 2) {
      String key = split[i];
      String value = split[i + 1];
      result.put(key, value);
    }
    return result;
  }

  protected Object deserializeObjectFromKeyValueString(String keyValueString, String elementSeparator,
      String keyValueSeparator) {
    Map<String, String> result = new HashMap<>();
    Arrays.asList(keyValueString.split(elementSeparator))
        .forEach(keyValue -> {
          String[] split = keyValue.split(keyValueSeparator);
          if (split.length != 2) {
            throw ExceptionHelper.illegalArgumentException(
                "Key value element '{}' with separator '{}' should have one " + "key and one value.", keyValue,
                keyValueSeparator);
          }
          result.put(split[0], split[1]);
        });
    return result;
  }

  protected Object getPathParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    try {
      return request.pathVariable(parameter.getName());
    } catch (IllegalArgumentException e) {
      if (Objects.nonNull(parameter.getSchema()) && Objects.nonNull(parameter.getSchema()
          .getDefault())) {
        return parameter.getSchema()
            .getDefault();
      }
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper.parameterValidationException(
            "No value provided for required path parameter " + "'{}'.", parameter.getName());
      }
    }
    return null;
  }

  protected Object getQueryParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    List<String> result = request.queryParams()
        .get(parameter.getName());

    if (Objects.isNull(result)) {
      if (Objects.nonNull(parameter.getSchema()) && Objects.nonNull(parameter.getSchema()
          .getDefault())) {
        result = new ArrayList<>();
      }
      if (parameter.getRequired() && Objects.isNull(result)) {
        throw OpenApiExceptionHelper.parameterValidationException("No value provided for required query parameter '{}'",
            parameter.getName());
      }
    }

    if ("array".equals(parameter.getSchema()
        .getType()) && parameter.getExplode()) {
      return result;
    }
    return !result.isEmpty() ? result.get(0) : null;
  }

  protected Object getHeaderParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    List<String> result = request.headers()
        .header(parameter.getName());

    if (result.isEmpty()) {
      if (Objects.nonNull(parameter.getSchema()) && Objects.nonNull(parameter.getSchema()
          .getDefault())) {
        return parameter.getSchema()
            .getDefault();
      }
      if (result.isEmpty() && parameter.getRequired()) {
        throw OpenApiExceptionHelper
            .parameterValidationException("No value provided for required header parameter '{}'", parameter.getName());
      }
    }

    return !result.isEmpty() ? result.get(0) : null;
  }
}
