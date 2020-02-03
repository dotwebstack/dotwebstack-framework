package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsExtension;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsType;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isEnvelope;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;

@Builder
public class ResponseTemplateBuilder {

  private static final String DEFAULT_CONTENT_TYPE_VENDOR_EXTENSION = "x-dws-default";

  private final OpenAPI openApi;

  private final List<String> xdwsStringTypes;

  private static boolean isRequired(Schema<?> schema, String property) {
    return schema == null || (Objects.nonNull(schema.getRequired()) && schema.getRequired()
        .contains(property));
  }

  public List<ResponseTemplate> buildResponseTemplates(@NonNull HttpMethodOperation httpMethodOperation) {

    return httpMethodOperation.getOperation()
        .getResponses()
        .entrySet()
        .stream()
        .flatMap(entry -> createResponses(openApi, entry.getKey(), entry.getValue(),
            DwsExtensionHelper.getDwsQueryName(httpMethodOperation.getOperation())).stream())
        .collect(Collectors.toList());
  }

  private List<ResponseTemplate> createResponses(OpenAPI openApi, String responseCode, ApiResponse apiResponse,
      String queryName) {

    Map<String, Header> headers = apiResponse.getHeaders();
    Content content = apiResponse.getContent();

    Map<String, ResponseHeader> responseHeaders = createResponseHeaders(headers);


    ResponseTemplate.ResponseTemplateBuilder responseTemplateBuilder = ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .responseHeaders(responseHeaders)
        .isDefault(true);


    if (Objects.isNull(content)) {
      return Collections.singletonList(responseTemplateBuilder.build());
    }

    return content.entrySet()
        .stream()
        .map(entry -> {
          MediaType mediaType = entry.getValue();

          return responseTemplateBuilder.mediaType(org.springframework.http.MediaType.valueOf(entry.getKey()))
              .responseObject(getResponseObject(openApi, responseCode, mediaType, queryName))
              .isDefault(isDefault(mediaType, mediaType.getExtensions()))
              .build();
        })
        .collect(Collectors.toList());
  }

  private Map<String, ResponseHeader> createResponseHeaders(Map<String, Header> headers) {
    if (Objects.isNull(headers)) {
      return Collections.emptyMap();
    }

    return headers.entrySet()
        .stream()
        .map(this::mapHeader)
        .collect(Collectors.toMap(ResponseHeader::getName, Function.identity()));
  }

  private ResponseHeader mapHeader(Map.Entry<String, Header> e) {
    Schema<?> schema = Objects.nonNull(e.getValue()
        .get$ref()) ? resolveSchema(openApi,
            e.getValue()
                .getSchema(),
            e.getValue()
                .get$ref())
            : e.getValue()
                .getSchema();

    return ResponseHeader.builder()
        .name(e.getKey())
        .defaultValue((String) schema.getDefault())
        .type(schema.getType())
        .jexlExpression((String) schema.getExtensions()
            .get(X_DWS_EXPR))
        .build();
  }

  private boolean isDefault(MediaType content, Map<String, Object> extensions) {
    return Objects.nonNull(extensions) && (boolean) content.getExtensions()
        .get(DEFAULT_CONTENT_TYPE_VENDOR_EXTENSION);
  }

  @SuppressWarnings("rawtypes")
  private ResponseObject getResponseObject(OpenAPI openApi, String responseCode, MediaType content, String queryName) {
    String ref = content.getSchema()
        .get$ref();

    Schema<?> schema = Objects.nonNull(ref) ? resolveSchema(openApi, content.getSchema()) : content.getSchema();
    ResponseObject responseObject = createResponseObject(queryName, schema, ref, true, false);

    Map<String, SchemaSummary> referenceMap = new HashMap<>();

    if (Objects.nonNull(ref)) {
      referenceMap.put(ref, responseObject.getSummary());
    }

    fillResponseObject(responseObject, openApi, referenceMap, new ArrayList<>(), responseCode);

    return responseObject;
  }

  @SuppressWarnings("rawtypes")
  private void fillResponseObject(ResponseObject responseObject, OpenAPI openApi,
      Map<String, SchemaSummary> referenceMap, List<String> parents, String responseCode) {
    Schema<?> oasSchema = responseObject.getSummary()
        .getSchema();
    parents.add(responseObject.getIdentifier());

    if (Objects.isNull(oasSchema.getType())) {
      throw invalidConfigurationException(
          "Found invalid schema for OAS response object '{}' for responseCode '{}': schema's cannot have type 'null'",
          responseObject.getIdentifier(), responseCode);
    }

    if (oasSchema instanceof ObjectSchema) {
      resolveObjectSchema(responseObject, openApi, referenceMap, parents, oasSchema, responseCode);
    } else if (oasSchema instanceof ArraySchema) {
      resolveArraySchema(responseObject, openApi, referenceMap, parents, oasSchema, responseCode);
    } else if (oasSchema instanceof ComposedSchema) {
      resolveComposedSchema(responseObject, openApi, referenceMap, parents, (ComposedSchema) oasSchema, responseCode);
    }
  }

  @SuppressWarnings("rawtypes")
  private void resolveComposedSchema(ResponseObject responseObject, OpenAPI openApi,
      Map<String, SchemaSummary> referenceMap, List<String> parents, ComposedSchema oasSchema, String responseCode) {
    if (Objects.nonNull(oasSchema.getOneOf()) || Objects.nonNull(oasSchema.getAnyOf())) {
      throw invalidConfigurationException("The use of oneOf and anyOf schema's is currently not supported");
    }

    List<ResponseObject> composedSchemas = oasSchema.getAllOf()
        .stream()
        .map(schema -> {
          String ref = null;
          if (Objects.nonNull(schema.get$ref())) {
            ref = schema.get$ref();
            schema = resolveSchema(openApi, schema);
          }

          if (!"object".equals(schema.getType())) {
            throw invalidConfigurationException("Field '{}' for response code '{}' is configured incorrectly,"
                + " allOf schema's only support object schema's", responseObject.getIdentifier(), responseCode);
          }

          if (referenceMap.containsKey(schema.get$ref())) {
            return createResponseObject(responseObject.getIdentifier(), referenceMap.get(schema.get$ref()));
          }

          ResponseObject composedSchema =
              createResponseObject(responseObject.getIdentifier(), schema, ref, true, false);

          if (Objects.nonNull(schema.get$ref())) {
            referenceMap.put(schema.get$ref(), composedSchema.getSummary());
          }

          fillResponseObject(composedSchema, openApi, referenceMap, new ArrayList<>(parents), responseCode);
          return composedSchema;
        })
        .collect(Collectors.toList());

    responseObject.getSummary()
        .setComposedOf(composedSchemas);
  }

  @SuppressWarnings("rawtypes")
  private void resolveArraySchema(ResponseObject responseObject, OpenAPI openApi,
      Map<String, SchemaSummary> referenceMap, List<String> parents, Schema<?> oasSchema, String responseCode) {
    String ref = ((ArraySchema) oasSchema).getItems()
        .get$ref();
    Schema<?> usedSchema =
        (Objects.nonNull(ref)) ? resolveSchema(openApi, oasSchema, ref) : ((ArraySchema) oasSchema).getItems();

    ResponseObject item;
    if (referenceMap.containsKey(ref)) {
      item = createResponseObject(responseObject.getIdentifier(), referenceMap.get(ref));
    } else {
      item = createResponseObject(responseObject.getIdentifier(), usedSchema, ref, true, isNillable(usedSchema));
      if (Objects.nonNull(ref)) {
        referenceMap.put(ref, item.getSummary());
      }
      fillResponseObject(item, openApi, referenceMap, new ArrayList<>(parents), responseCode);
    }

    responseObject.getSummary()
        .setItems(ImmutableList.of(item));
  }

  @SuppressWarnings("rawtypes")
  private void resolveObjectSchema(ResponseObject responseObject, OpenAPI openApi,
      Map<String, SchemaSummary> referenceMap, List<String> parents, Schema<?> oasSchema, String responseCode) {
    SchemaSummary responseSummary = responseObject.getSummary();

    if (Objects.isNull(oasSchema.getProperties())) {
      return;
    }

    List<ResponseObject> children = oasSchema.getProperties()
        .entrySet()
        .stream()
        .map(entry -> {
          String propId = entry.getKey();
          Schema<?> propSchema = entry.getValue();
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

          fillResponseObject(child, openApi, referenceMap, new ArrayList<>(parents), responseCode);
          return child;
        })
        .collect(Collectors.toList());

    responseObject.getSummary()
        .setChildren(children);
  }

  private ResponseObject createResponseObject(String identifier, SchemaSummary summary) {
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(summary)
        .build();
  }

  private ResponseObject createResponseObject(String identifier, Schema<?> schema, String ref, boolean isRequired,
      boolean isNillable) {
    Optional<String> xdwsType = getXdwsType(schema);
    if (xdwsType.isPresent() && this.xdwsStringTypes.contains(xdwsType.get())) {
      return ResponseObject.builder()
          .identifier(identifier)
          .summary(createResponseObject(new StringSchema(), ref, isRequired, isNillable))
          .build();
    }
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(createResponseObject(schema, ref, isRequired, isNillable))
        .build();
  }

  private SchemaSummary createResponseObject(Schema<?> schema, String ref, boolean isRequired, boolean isNillable) {
    return SchemaSummary.builder()
        .isEnvelope(isEnvelope(schema))
        .type(Objects.nonNull(schema.getType()) ? schema.getType() : "object")
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

  protected static Optional<String> getXdwsType(Schema<?> schema) {
    if (Objects.nonNull(schema.getExtensions())) {
      Object type = schema.getExtensions()
          .get(X_DWS_TYPE);
      if (Objects.nonNull(type)) {
        return Optional.of((String) type);
      }
    }
    return Optional.empty();
  }
}
