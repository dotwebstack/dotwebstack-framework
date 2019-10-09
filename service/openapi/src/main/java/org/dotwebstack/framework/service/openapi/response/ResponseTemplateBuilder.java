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
import java.util.ArrayList;
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

  @SuppressWarnings("rawtypes")
  private ResponseTemplate createResponseObjectTemplate(OpenAPI openApi, String responseCode, String mediaType,
      io.swagger.v3.oas.models.media.MediaType content, String queryName) {
    String ref = content.getSchema()
        .get$ref();

    Schema schema = Objects.nonNull(ref) ? resolveSchema(openApi, content.getSchema()) : content.getSchema();
    ResponseObject root = createResponseObject(queryName, schema, ref, true, false);

    Map<String, SchemaSummary> referenceMap = new HashMap<>();

    if (Objects.nonNull(ref)) {
      referenceMap.put(ref, root.getSummary());
    }

    fillResponseObject(root, openApi, referenceMap, new ArrayList<>());

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediaType)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private void fillResponseObject(ResponseObject responseObject, OpenAPI openApi,
      Map<String, SchemaSummary> referenceMap, List<String> parents) {
    Schema<?> oasSchema = responseObject.getSummary()
        .getSchema();
    SchemaSummary responseSummary = responseObject.getSummary();
    parents.add(responseObject.getIdentifier());

    if (oasSchema instanceof ObjectSchema) {
      List<ResponseObject> children = oasSchema.getProperties()
          .entrySet()
          .stream()
          .map(entry -> {
            String propId = entry.getKey();
            Schema propSchema = entry.getValue();
            String ref = null;
            if (Objects.nonNull(propSchema.get$ref())) {
              ref = propSchema.get$ref();
              propSchema = resolveSchema(openApi, propSchema);
            }
            boolean childRequired = isRequired(responseSummary.getSchema(), propId);
            boolean childNillable = isNillable(propSchema);

            if (referenceMap.containsKey(propSchema.get$ref())) {
              return createResponseObject(propId, referenceMap.get(propSchema.get$ref()));
            }

            ResponseObject child = createResponseObject(propId, propSchema, ref, childRequired, childNillable);

            if (Objects.nonNull(propSchema.get$ref())) {
              referenceMap.put(propSchema.get$ref(), child.getSummary());
            }

            fillResponseObject(child, openApi, referenceMap, new ArrayList<>(parents));
            return child;
          })
          .collect(Collectors.toList());

      responseObject.getSummary()
          .setChildren(children);
    } else if (oasSchema instanceof ArraySchema) {
      String ref = ((ArraySchema) oasSchema).getItems()
          .get$ref();
      Schema usedSchema =
          (Objects.nonNull(ref)) ? resolveSchema(openApi, oasSchema, ref) : ((ArraySchema) oasSchema).getItems();

      ResponseObject item;
      if (referenceMap.containsKey(ref)) {
        item = createResponseObject(responseObject.getIdentifier(), referenceMap.get(ref));
      } else {
        item = createResponseObject(responseObject.getIdentifier(), usedSchema, ref, true, isNillable(usedSchema));
        if (Objects.nonNull(ref)) {
          referenceMap.put(ref, item.getSummary());
        }
        fillResponseObject(item, openApi, referenceMap, new ArrayList<>(parents));
      }

      responseObject.getSummary()
          .setItems(ImmutableList.of(item));
    }
  }

  private ResponseObject createResponseObject(String identifier, SchemaSummary summary) {
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(summary)
        .build();
  }

  private ResponseObject createResponseObject(String identifier, Schema<?> schema, String ref, boolean isRequired,
      boolean isNillable) {
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(createResponseObject(schema, ref, isRequired, isNillable))
        .build();
  }

  private SchemaSummary createResponseObject(Schema<?> schema, String ref, boolean isRequired, boolean isNillable) {
    return SchemaSummary.builder()
        .isEnvelope(isEnvelope(schema))
        .type(schema.getType())
        .dwsType(getDwsType(schema))
        .nillable(isNillable)
        .dwsExpr(getDwsExpression(schema))
        .required(isRequired)
        .schema(schema)
        .ref(ref)
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
