package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsExtension;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsType;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isEnvelope;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveRequestBody;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;

@Builder
public class ResponseTemplateBuilder {

  private final OpenAPI openApi;

  private static boolean isRequired(Schema<?> schema, String property) {
    return schema == null || (Objects.nonNull(schema.getRequired()) && schema.getRequired()
        .contains(property));
  }

  public List<ResponseTemplate> buildResponseTemplates(@NonNull HttpMethodOperation httpMethodOperation) {
    List<ResponseTemplate> responses = httpMethodOperation.getOperation()
        .getResponses()
        .entrySet()
        .stream()
        .flatMap(entry -> createResponses(openApi, entry.getKey(), entry.getValue(), httpMethodOperation.getName(),
            httpMethodOperation.getHttpMethod()
                .name(),
            resolveRequestBody(openApi, httpMethodOperation.getOperation()
                .getRequestBody()),
            (String) httpMethodOperation.getOperation()
                .getExtensions()
                .get(X_DWS_QUERY)).stream())
        .collect(Collectors.toList());

    long successResponseCount = responses.stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .count();
    if (successResponseCount != 1) {
      throw invalidConfigurationException(
          "Expected exactly one response within the 200 range for path '{}' with method '{}'.",
          httpMethodOperation.getName(), httpMethodOperation.getHttpMethod());
    }
    return responses;
  }

  private List<ResponseTemplate> createResponses(OpenAPI openApi, String responseCode, ApiResponse apiResponse,
      String pathName, String methodName, RequestBody requestBody, String queryName) {
    validateMediaType(responseCode, apiResponse.getContent(), pathName, methodName);
    if (requestBody != null) {
      validateMediaType(responseCode, requestBody.getContent(), pathName, methodName);
    }
    return apiResponse.getContent()
        .entrySet()
        .stream()
        .map(entry -> createResponseObjectTemplate(openApi, responseCode, entry.getKey(), entry.getValue(), queryName))
        .collect(Collectors.toList());
  }

  private void validateMediaType(@NonNull String responseCode, @NonNull Content content, @NonNull String pathName,
      @NonNull String methodName) {
    if (content.keySet()
        .size() != 1) {
      throw invalidOpenApiConfigurationException(
          "Expected exactly one MediaType for path '{}' with method '{}' and response code '{}'.", pathName, methodName,
          responseCode);
    }
    List<String> unsupportedMediaTypes = content.keySet()
        .stream()
        .filter(name -> !name.matches("application/(.)*(\\\\+)?json"))
        .collect(Collectors.toList());
    if (!unsupportedMediaTypes.isEmpty()) {
      throw invalidOpenApiConfigurationException(
          "Unsupported MediaType(s) '{}' for path '{}' with method '{}' and response code '{}'.", unsupportedMediaTypes,
          pathName, methodName, responseCode);
    }
  }

  @SuppressWarnings("rawtypes")
  private ResponseTemplate createResponseObjectTemplate(OpenAPI openApi, String responseCode, String mediaType,
      io.swagger.v3.oas.models.media.MediaType content, String queryName) {
    String ref = content.getSchema()
        .get$ref();

    ResponseObject root;
    Map<ObjectSchema, ResponseSchema> referenceMap = new HashMap<>();
    if (Objects.nonNull(ref)) {
      root = createResponseObject(openApi, queryName, resolveSchema(openApi, content.getSchema()), true, false,
          referenceMap);
    } else {
      root = createResponseObject(openApi, queryName, content.getSchema(), true, false, referenceMap);
    }

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediaType)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, Schema schema, boolean isRequired,
      boolean isNillable, Map<ObjectSchema, ResponseSchema> referenceMap) {
    if (schema.get$ref() != null) {
      return createResponseObject(openApi, identifier, resolveSchema(openApi, schema), isRequired, isNillable,
          referenceMap);
    } else if (schema instanceof ObjectSchema) {
      return createResponseObject(openApi, identifier, (ObjectSchema) schema, isRequired, isNillable, referenceMap);
    } else if (schema instanceof ArraySchema) {
      return createResponseObject(openApi, identifier, (ArraySchema) schema, isRequired, isNillable, referenceMap);
    } else {
      return createResponseFromSchema(identifier, schema, isRequired, isNillable);
    }
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, ObjectSchema schema,
      boolean isRequired, boolean isNillable, Map<ObjectSchema, ResponseSchema> referenceMap) {
    Map<String, Schema> schemaProperties = schema.getProperties();

    ResponseObject result = createResponseFromSchema(identifier, schema, isRequired, isNillable);
    referenceMap.put(schema, result.getSchema());

    List<ResponseObject> children = schemaProperties.entrySet()
        .stream()
        .map(entry -> {
          String propId = entry.getKey();
          Schema propSchema = entry.getValue();
          boolean childRequired = isRequired(schema, propId);
          boolean childNillable = isNillable(propSchema);
          if (referenceMap.containsKey(propSchema)) {
            return createResponseFromSchema(propId, referenceMap.get(propSchema));
          }
          return createResponseObject(openApi, propId, propSchema, childRequired, childNillable, referenceMap);
        })
        .collect(Collectors.toList());

    result.getSchema()
        .setChildren(children);
    return result;
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, ArraySchema schema,
      boolean isRequired, boolean isNillable, Map<ObjectSchema, ResponseSchema> referenceMap) {
    ResponseObject result = createResponseFromSchema(identifier, schema, isRequired, isNillable);

    String ref = schema.getItems()
        .get$ref();

    Schema usedSchema = (Objects.nonNull(ref)) ? resolveSchema(openApi, schema, ref) : schema.getItems();
    ResponseObject item;
    if (referenceMap.containsKey(usedSchema)) {
      item = createResponseFromSchema(identifier, referenceMap.get(usedSchema));
    } else {
      item = createResponseObject(openApi, identifier, usedSchema, true, isNillable(usedSchema), referenceMap);
      if (usedSchema instanceof ObjectSchema) {
        referenceMap.put((ObjectSchema) usedSchema, item.getSchema());
      }
    }

    result.getSchema()
        .setItems(ImmutableList.of(item));
    return result;
  }

  private ResponseObject createResponseFromSchema(String identifier, ResponseSchema responseSchema) {
    return ResponseObject.builder()
        .identifier(identifier)
        .schema(responseSchema)
        .build();
  }

  private ResponseObject createResponseFromSchema(String identifier, Schema<?> schema, boolean isRequired,
      boolean isNillable) {
    return ResponseObject.builder()
        .identifier(identifier)
        .schema(createResponseSchema(schema, isRequired, isNillable))
        .build();
  }

  private ResponseSchema createResponseSchema(Schema<?> schema, boolean isRequired, boolean isNillable) {
    return ResponseSchema.builder()
        .isEnvelope(isEnvelope(schema))
        .type(schema.getType())
        .dwsType(getDwsType(schema))
        .nillable(isNillable)
        .dwsExpr(getDwsExpression(schema))
        .required(isRequired)
        .schema(schema)
        .build();
  }

  private boolean isNillable(Schema<?> schema) {
    return schema != null && (isEnvelope(schema) || Boolean.TRUE.equals(schema.getNullable()));
  }

  private String getDwsExpression(Schema<?> schema) {
    Object result = getDwsExtension(schema, X_DWS_EXPR);

    if (Objects.isNull(result)) {
      return null;
    }

    if (!(result instanceof String)) {
      throw invalidConfigurationException("Value of extension '{}' should be a string.", X_DWS_EXPR);
    }
    if (!Objects.equals("string", schema.getType())) {
      throw invalidConfigurationException("Extension '{}' is only allowed for string types.", X_DWS_EXPR);
    }

    return (String) result;
  }
}
