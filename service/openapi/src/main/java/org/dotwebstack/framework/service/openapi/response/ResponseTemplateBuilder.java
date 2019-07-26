package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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
    ObjectSchema schema = getSchemaReference(ref, openApi);

    ResponseObject root = createResponseObject(openApi, ref, schema, true, false);

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediatype)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, ObjectSchema schema,
      boolean isRequired, boolean isNillable) {
    Map<String, Schema> schemaProperties = schema.getProperties();
    List<ResponseObject> children = schemaProperties.entrySet()
        .stream()
        .map(entry -> {
          String propId = entry.getKey();
          Schema propSchema = entry.getValue();
          return createResponseObjectFromProperty(openApi, propId, schema, propSchema);
        })
        .collect(Collectors.toList());
    return ResponseObject.builder()
        .identifier(identifier)
        .type(schema.getType())
        .children(children)
        .nillable(isNillable)
        .required(isRequired)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObjectFromProperty(OpenAPI openApi, String identifier, ObjectSchema schema,
      Schema propertySchema) {
    boolean required = isRequired(schema, identifier);
    boolean nillable = isNillable(schema);
    if (propertySchema.get$ref() != null) {
      String ref = propertySchema.get$ref();
      ObjectSchema objectSchema = getSchemaReference(ref, openApi);

      return createResponseObject(openApi, identifier, objectSchema, required, nillable);
    } else if (propertySchema instanceof ArraySchema) {
      String ref = ((ArraySchema) propertySchema).getItems()
          .get$ref();
      ObjectSchema objectSchema = getSchemaReference(ref, openApi);

      ResponseObject item = createResponseObject(openApi, identifier, objectSchema, required, nillable);
      return ResponseObject.builder()
          .identifier(identifier)
          .type(propertySchema.getType())
          .nillable(nillable)
          .required(required)
          .items(ImmutableList.of(item))
          .build();
    } else {
      return ResponseObject.builder()
          .identifier(identifier)
          .type(propertySchema.getType())
          .nillable(nillable)
          .required(required)
          .build();
    }
  }

  private boolean isNillable(ObjectSchema schema) {
    return schema == null ? false : Boolean.FALSE == schema.getNullable();
  }

  private static boolean isRequired(Schema<?> schema, String property) {
    return schema == null || schema.getRequired()
        .contains(property);
  }

  @SuppressWarnings("rawtypes")
  private ObjectSchema getSchemaReference(String ref, OpenAPI openApi) {
    String[] refPath = ref.split("/");
    Schema result = openApi.getComponents()
        .getSchemas()
        .get(refPath[refPath.length - 1]);

    if (Objects.isNull(result)) {
      throw invalidConfigurationException("Schema '{}' not found in configuration.", ref);
    }

    return (ObjectSchema) result;
  }
}
