package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

@Builder
public class ResponseTemplateBuilder {
  private final OpenAPI openApi;

  public List<ResponseTemplate> buildResponseTemplates(@NonNull String pathName, @NonNull String methodName,
      @NonNull Operation operation) {
    List<ResponseTemplate> responses = operation.getResponses()
        .entrySet()
        .stream()
        .flatMap(entry -> createResponses(openApi, entry.getKey(), entry.getValue(), pathName, methodName).stream())
        .collect(Collectors.toList());

    long successResponseCount = responses.stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .count();
    if (successResponseCount != 1) {
      throw invalidConfigurationException(
          "Expected exactly one response within the 200 range for path '{}' with method '{}'.", pathName, methodName);
    }
    return responses;
  }

  private List<ResponseTemplate> createResponses(OpenAPI openApi, String responseCode, ApiResponse apiResponse,
      String pathName, String methodName) {
    validateMediaType(responseCode, apiResponse, pathName, methodName);
    return apiResponse.getContent()
        .entrySet()
        .stream()
        .map(entry -> createResponseObject(openApi, responseCode, entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  private void validateMediaType(String responseCode, ApiResponse apiResponse, String pathName, String methodName) {
    if (apiResponse.getContent()
        .keySet()
        .size() != 1) {
      throw ExceptionHelper.invalidConfigurationException(
          "Expected exactly one MediaType for path '{}' with method '{}' and response code '{}'.", pathName, methodName,
          responseCode);
    }
    List<String> unsupportedMediaTypes = apiResponse.getContent()
        .keySet()
        .stream()
        .filter(name -> !name.matches("application/.*\\+json"))
        .collect(Collectors.toList());
    if (!unsupportedMediaTypes.isEmpty()) {
      throw ExceptionHelper.invalidConfigurationException(
          "Unsupported MediaType(s) '{}' for path '{}' with method '{}' and response code '{}'.", unsupportedMediaTypes,
          pathName, methodName, responseCode);
    }
  }

  @SuppressWarnings("rawtypes")
  private ResponseTemplate createResponseObject(OpenAPI openApi, String responseCode, String mediatype,
      io.swagger.v3.oas.models.media.MediaType content) {
    String ref = content.getSchema()
        .get$ref();
    Schema schema = getSchemaReference(ref, openApi);

    ResponseObject root = createResponseObject(openApi, ref, schema, null);

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediatype)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, Schema schema, Schema parent) {
    Map<String, Schema> schemaProperties = schema.getProperties();

    List<ResponseObject> children = null;
    if (Objects.nonNull(schemaProperties)) {
      children = schemaProperties.entrySet()
          .stream()
          .map(entry -> createResponseObject(openApi, entry.getKey(), entry.getValue(), schema))
          .collect(Collectors.toList());
    }

    List<ResponseObject> items = null;
    if (schema instanceof ArraySchema) {
      Schema item = ((ArraySchema) schema).getItems();
      String ref = item.get$ref();
      Schema child = getSchemaReference(ref, openApi);
      items = Collections.singletonList(createResponseObject(openApi, ref, child, null));
    }

    return ResponseObject.builder()
        .identifier(identifier)
        .type(schema.getType())
        .children(children)
        .items(items)
        .nillable(Objects.isNull(schema.getNullable()) ? Boolean.FALSE : schema.getNullable())
        .required(Objects.isNull(parent) || parent.getRequired()
            .contains(identifier))
        .build();
  }

  @SuppressWarnings("rawtypes")
  private Schema getSchemaReference(String ref, OpenAPI openApi) {
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
