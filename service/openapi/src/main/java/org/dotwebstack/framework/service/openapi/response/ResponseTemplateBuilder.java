package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsExtension;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsType;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isEnvelope;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TEMPLATE;
import static org.dotwebstack.framework.service.openapi.helper.SchemaUtils.getSchemaReference;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
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
            httpMethodOperation.getOperation()
                .getRequestBody()).stream())
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
      String pathName, String methodName, RequestBody requestBody) {
    validateMediaType(responseCode, apiResponse.getContent(), pathName, methodName);
    if (requestBody != null) {
      validateMediaType(responseCode, requestBody.getContent(), pathName, methodName);
    }
    return apiResponse.getContent()
        .entrySet()
        .stream()
        .map(entry -> createResponseObject(openApi, responseCode, entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  private void validateMediaType(String responseCode, Content content, String pathName, String methodName) {
    if (content.keySet()
        .size() != 1) {
      throw ExceptionHelper.invalidConfigurationException(
          "Expected exactly one MediaType for path '{}' with method '{}' and response code '{}'.", pathName, methodName,
          responseCode);
    }
    List<String> unsupportedMediaTypes = content.keySet()
        .stream()
        .filter(name -> !name.matches("application/(.)*(\\\\+)?json"))
        .collect(Collectors.toList());
    if (!unsupportedMediaTypes.isEmpty()) {
      throw ExceptionHelper.invalidConfigurationException(
          "Unsupported MediaType(s) '{}' for path '{}' with method '{}' and response code '{}'.", unsupportedMediaTypes,
          pathName, methodName, responseCode);
    }
  }

  @SuppressWarnings("rawtypes")
  private ResponseTemplate createResponseObject(OpenAPI openApi, String responseCode, String mediaType,
      io.swagger.v3.oas.models.media.MediaType content) {
    String ref = content.getSchema()
        .get$ref();

    ResponseObject root;
    if (Objects.nonNull(ref)) {
      Schema schema = getSchemaReference(ref, openApi);
      root = createResponseObject(openApi, ref, schema, true, false);
    } else {
      root = createResponseObject(openApi, null, content.getSchema(), true, false);
    }

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediaType)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, Schema schema, boolean isRequired,
      boolean isNillable) {
    if (schema.get$ref() != null) {
      return createResponseObject(openApi, identifier, getSchemaReference(schema.get$ref(), openApi), isRequired,
          isNillable);
    } else if (schema instanceof ObjectSchema) {
      return createResponseObject(openApi, identifier, (ObjectSchema) schema, isRequired, isNillable);
    } else if (schema instanceof ArraySchema) {
      return createResponseObject(openApi, identifier, (ArraySchema) schema, isRequired, isNillable);
    } else {

      return ResponseObject.builder()
          .identifier(identifier)
          .isEnvelope(isEnvelope(schema))
          .type(schema.getType())
          .dwsType(getDwsType(schema))
          .nillable(isNillable)
          .required(isRequired)
          .dwsTemplate(getDwsTemplate(schema))
          .build();
    }
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
          boolean childRequired = isRequired(schema, propId);
          boolean childNillable = isNillable(propSchema);
          return createResponseObject(openApi, propId, propSchema, childRequired, childNillable);
        })
        .collect(Collectors.toList());
    return ResponseObject.builder()
        .identifier(identifier)
        .isEnvelope(isEnvelope(schema))
        .type(schema.getType())
        .dwsType(getDwsType(schema))
        .children(children)
        .nillable(isNillable)
        .dwsTemplate(getDwsTemplate(schema))
        .required(isRequired)
        .build();
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject createResponseObject(OpenAPI openApi, String identifier, ArraySchema schema,
      boolean isRequired, boolean isNillable) {
    String ref = schema.getItems()
        .get$ref();
    ResponseObject item;
    if (Objects.nonNull(ref)) {
      Schema refSchema = getSchemaReference(ref, openApi);
      item = createResponseObject(openApi, identifier, refSchema, true, isNillable(refSchema));
    } else {
      item = createResponseObject(openApi, identifier, schema.getItems(), true, false);
    }
    return ResponseObject.builder()
        .identifier(identifier)
        .isEnvelope(isEnvelope(schema))
        .type(schema.getType())
        .dwsType(getDwsType(schema))
        .items(ImmutableList.of(item))
        .nillable(isNillable)
        .dwsTemplate(getDwsTemplate(schema))
        .required(isRequired)
        .build();

  }

  private boolean isNillable(Schema<?> schema) {
    return schema != null && (isEnvelope(schema) || Boolean.TRUE.equals(schema.getNullable()));
  }

  private String getDwsTemplate(Schema<?> schema) {
    Object result = getDwsExtension(schema, X_DWS_TEMPLATE);

    if (Objects.isNull(result)) {
      return null;
    }

    if (!(result instanceof String)) {
      throw ExceptionHelper.invalidConfigurationException("Value of extension '{}' should be a string.",
          X_DWS_TEMPLATE);
    }
    if (!Objects.equals("string", schema.getType())) {
      throw ExceptionHelper.invalidConfigurationException("Extension '{}' is only allowed for string types.",
          X_DWS_TEMPLATE);
    }

    return (String) result;
  }
}
