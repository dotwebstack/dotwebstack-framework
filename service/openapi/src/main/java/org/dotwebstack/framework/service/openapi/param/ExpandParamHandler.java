package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.supportsDwsType;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPAND_TYPE;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.JsonNodeUtils;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class ExpandParamHandler extends DefaultParamHandler {

  public ExpandParamHandler(OpenAPI openApi) {
    super(openApi);
  }

  @Override
  public boolean supports(Parameter parameter) {
    return supportsDwsType(parameter, X_DWS_EXPAND_TYPE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull Parameter parameter,
      @NonNull ResponseSchemaContext responseSchemaContext) {
    Optional<Object> expandValueOptional = super.getValue(request, parameter, responseSchemaContext);

    if (expandValueOptional.isPresent()) {
      List<String> allValues = new ArrayList<>();
      ((List<String>) expandValueOptional.get()).forEach(value -> {
        String[] path = value.split("\\.");
        StringJoiner pathBuilder = new StringJoiner(".");
        Stream.of(path)
            .forEach(pathElement -> {
              pathBuilder.add(pathElement);
              if (!allValues.contains(pathBuilder.toString())) {
                allValues.add(pathBuilder.toString());
              }
            });
      });
      Collections.sort(allValues);
      return Optional.of(allValues);
    }
    return expandValueOptional;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void validate(@NonNull GraphQlField graphQlField, @NonNull Parameter parameter, @NonNull String pathName) {
    Schema schema = parameter.getSchema();

    switch (schema.getType()) {
      case ARRAY_TYPE:
        if (Objects.nonNull(schema.getDefault())) {
          ((ArrayList<String>) Objects.requireNonNull(JsonNodeUtils.toObject((ArrayNode) schema.getDefault())))
              .forEach(defaultValue -> {
                validateExpandParam(graphQlField, defaultValue, pathName);
                validateEnumValues(defaultValue, parameter);
              });
        }
        ((ArraySchema) schema).getItems()
            .getEnum()
            .forEach((enumParam -> validateExpandParam(graphQlField, (String) enumParam, pathName)));
        break;
      case STRING_TYPE:
        if (Objects.nonNull(schema.getDefault())) {
          validateExpandParam(graphQlField, ((StringSchema) schema).getDefault(), pathName);
          validateEnumValues(((StringSchema) schema).getDefault(), parameter);
        }
        ((StringSchema) schema).getEnum()
            .forEach(enumParam -> validateExpandParam(graphQlField, enumParam, pathName));
        break;
      default:
        throw invalidOpenApiConfigurationException(
            "Expand parameter '{}' can only be of type array or string for path '{}'", parameter.getName(), pathName);
    }
  }

  @Override
  public void validate(GraphQlField graphQlField, String fieldName, String pathName) {
    if (graphQlField.getFields()
        .stream()
        .noneMatch(field -> field.getName()
            .equals(fieldName))) {
      throw invalidOpenApiConfigurationException(
          "No field with name '{}' was found on GraphQL field '{}' for path '{}'", fieldName, graphQlField.getName(),
          pathName);
    }
  }

  private void validateExpandParam(GraphQlField graphQlField, String expandValue, String pathName) {
    String[] pathParams = expandValue.split("\\.");
    validate(graphQlField, pathParams[0], pathName);

    if (pathParams.length > 1) {
      GraphQlField childField = graphQlField.getFields()
          .stream()
          .filter(field -> field.getName()
              .equals(pathParams[0]))
          .findFirst()
          .orElseThrow(() -> invalidOpenApiConfigurationException(
              "No field with name '{}' was found on GraphQL field '{}' for pathName '{}'", pathParams[0],
              graphQlField.getName(), pathName));
      validateExpandParam(childField, String.join(".", ArrayUtils.remove(pathParams, 0)), pathName);
    }
  }

  @Override
  public String getParameterName(Parameter param) {
    return X_DWS_EXPANDED_PARAMS;
  }
}
