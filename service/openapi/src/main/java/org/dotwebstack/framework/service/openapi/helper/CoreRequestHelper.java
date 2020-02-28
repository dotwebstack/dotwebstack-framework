package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public class CoreRequestHelper {

  private CoreRequestHelper() {}

  public static Set<String> getParameterNamesOfType(List<Parameter> params, String type) {
    return params.stream()
        .filter(parameter -> Objects.equals(parameter.getIn(), type))
        .map(Parameter::getName)
        .collect(Collectors.toSet());
  }

  public static void validateParameterExistence(String type, Set<String> schemaParams, Set<String> givenParams) {
    List<String> nonExistentVariables = givenParams.stream()
        .filter(parameter -> !schemaParams.contains(parameter))
        .collect(Collectors.toList());

    if (!nonExistentVariables.isEmpty()) {
      throw invalidConfigurationException("The following request {} parameters are not allowed on this endpoint: {}",
          type, nonExistentVariables);
    }
  }

  public static void validateRequestBodyNonexistent(ServerRequest request) {
    Mono<?> mono = request.bodyToMono(String.class);
    Object value = mono.block();
    if (Objects.nonNull(value)) {
      throw invalidConfigurationException("A request body is not allowed for this request");
    }
  }

  public static void validateResponseMediaTypesAreConfigured(List<MediaType> responseTemplatesList) {
    if (responseTemplatesList.isEmpty()) {
      throw unsupportedOperationException("No configured responses with mediatypes found within the 200 range.");
    }
  }

  public static void validateRequiredField(GraphQlField graphQlField, String requiredField, String dwsQueryName) {
    String[] fields = requiredField.split("\\.");
    validate(graphQlField, fields[0], dwsQueryName);

    if (fields.length > 1) {
      GraphQlField childField = graphQlField.getFields()
          .stream()
          .filter(field -> field.getName()
              .equals(fields[0]))
          .findFirst()
          .orElseThrow(() -> invalidOpenApiConfigurationException(
              "Required field '{}' was not found on GraphQL field '{}' for x-dws-query '{}'", fields[0],
              graphQlField.getName(), dwsQueryName));
      validateRequiredField(childField, String.join(".", ArrayUtils.remove(fields, 0)), dwsQueryName);
    }
  }

  public static void validate(GraphQlField graphQlField, String requiredField, String dwsQueryName) {
    if (graphQlField.getFields()
        .stream()
        .noneMatch(field -> field.getName()
            .equals(requiredField))) {
      throw invalidOpenApiConfigurationException(
          "Required field '{}' was not found on GraphQL field '{}' for x-dws-query '{}'", requiredField,
          graphQlField.getName(), dwsQueryName);
    }
  }

  public static Map<String, Object> addEvaluatedDwsParameters(Map<String, Object> inputParams,
      Map<String, String> dwsParameters, ServerRequest request, JexlHelper jexlHelper) {
    JexlContext jexlContext = buildJexlContext(request, inputParams);
    Map<String, Object> allParams = new HashMap<>(inputParams);
    dwsParameters.forEach((name, valueExpr) -> jexlHelper.evaluateExpression(valueExpr, jexlContext, Object.class)
        .ifPresent(value -> allParams.put(name, value)));
    return allParams;
  }

  private static JexlContext buildJexlContext(ServerRequest request, Map<String, Object> inputParams) {
    MapContext mapContext = new MapContext();
    mapContext.set(DwsExtensionHelper.DWS_QUERY_JEXL_CONTEXT_REQUEST, request);
    mapContext.set(DwsExtensionHelper.DWS_QUERY_JEXL_CONTEXT_PARAMS, inputParams);
    return mapContext;
  }
}
