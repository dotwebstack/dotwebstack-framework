package org.dotwebstack.framework.service.openapi.param;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.springframework.web.reactive.function.server.ServerRequest;

public class DefaultParamHandler implements ParamHandler {
  @Override
  public boolean canHandle(Parameter parameter) {
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

    return Optional.ofNullable(deserialize(parameter, paramValue));
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

  private Object deserialize(Parameter parameter, Object paramValue) {
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

  private Object deserializeObject(Parameter parameter, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == Parameter.StyleEnum.SIMPLE && !explode) {
      return deserializeObjectFromKeyValueString((String) paramValue, ",");
    } else if (style == Parameter.StyleEnum.SIMPLE && explode) {
      return deserializeObjectFromKeyValueString((String) paramValue, ",", "=");
    }
    return paramValue;
  }

  private Object deserializeObjectFromKeyValueString(String keyValueString, String separator) {
    String[] split = keyValueString.split(",");
    if (split.length % 2 != 0) {
      throw ExceptionHelper.illegalArgumentException(
          "Key value string '{}' with separator '{}' should contain an " + "even number of elements.", keyValueString,
          separator);
    }
    Map<String, String> result = new HashMap<>();
    IntStream.iterate(0, i -> i < split.length, i -> i + 2)
        .forEach(i -> {
          String key = split[i];
          String value = split[i + 1];
          result.put(key, value);
        });
    return result;
  }

  private Object deserializeObjectFromKeyValueString(String keyValueString, String elementSeparator,
      String keyValueSeparator) {
    Map<String, String> result = new HashMap<>();
    Arrays.asList(keyValueString.split(elementSeparator))
        .stream()
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

  private Object deserializeArray(Parameter parameter, Object paramValue) {
    Parameter.StyleEnum style = parameter.getStyle();
    boolean explode = parameter.getExplode();

    if (style == Parameter.StyleEnum.SIMPLE && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == Parameter.StyleEnum.FORM && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(","));
    } else if (style == Parameter.StyleEnum.SPACEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split(" "));
    } else if (style == Parameter.StyleEnum.PIPEDELIMITED && !explode) {
      return ImmutableList.copyOf(((String) paramValue).split("\\|"));
    } else {
      return paramValue;
    }
  }

  private Object getPathParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    try {
      return request.pathVariable(parameter.getName());
    } catch (IllegalArgumentException e) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper.parameterValidationException(
            "No value provided for required path parameter " + "'{}'.", parameter.getName());
      }
    }
    return null;
  }

  private Object getQueryParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    Object value = request.queryParams()
        .get(parameter.getName());

    if (parameter.getRequired() && Objects.isNull(value)) {
      throw OpenApiExceptionHelper.parameterValidationException("No value provided for required query parameter '{}'",
          parameter.getName());
    }
    return value;
  }

  private Object getHeaderParam(Parameter parameter, ServerRequest request) throws ParameterValidationException {
    List<String> result = request.headers()
        .header(parameter.getName());
    if (result.isEmpty()) {
      if (parameter.getRequired()) {
        throw OpenApiExceptionHelper
            .parameterValidationException("No value provided for required header parameter '{}'", parameter.getName());
      }
    }
    if (!"array".equals(parameter.getSchema()
        .getType())) {
      return !result.isEmpty() ? result.get(0) : null;
    } else {
      return result;
    }
  }
}
