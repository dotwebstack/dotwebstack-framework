package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsExtension;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasFieldBuilder;

@Builder
public class ResponseTemplateBuilder {

  private static final String DEFAULT_CONTENT_TYPE_VENDOR_EXTENSION = "x-dws-default";

  private static final String TEMPLATE_NAME_VENDOR_EXTENSION = "x-dws-template";

  private static final String OBJECT_TYPE = "object";

  private final OpenAPI openApi;

  private final List<String> xdwsStringTypes;

  public List<ResponseTemplate> buildResponseTemplates(@NonNull HttpMethodOperation httpMethodOperation) {

    return httpMethodOperation.getOperation()
        .getResponses()
        .entrySet()
        .stream()
        .flatMap(entry -> createResponses(openApi, entry.getKey(), entry.getValue(),
            getDwsQueryName(httpMethodOperation.getOperation()).orElse(null)).stream())
        .collect(Collectors.toList());
  }

  private List<ResponseTemplate> createResponses(OpenAPI openApi, String responseCode, ApiResponse apiResponse,
      String queryName) {

    Map<String, Header> headers = apiResponse.getHeaders();
    var content = apiResponse.getContent();

    Map<String, ResponseHeader> responseHeaders = createResponseHeaders(headers, queryName);

    var responseTemplateBuilder = ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .responseHeaders(responseHeaders)
        .isDefault(true);

    if (Objects.isNull(content)) {
      return Collections.singletonList(responseTemplateBuilder.build());
    }

    return content.entrySet()
        .stream()
        .map(mapToResponseTemplate(openApi, responseTemplateBuilder))
        .collect(Collectors.toList());
  }

  private Function<Map.Entry<String, MediaType>, ResponseTemplate> mapToResponseTemplate(OpenAPI openApi,
      ResponseTemplate.ResponseTemplateBuilder responseTemplateBuilder) {
    return entry -> {
      var mediaType = entry.getValue();

      return responseTemplateBuilder.mediaType(org.springframework.http.MediaType.valueOf(entry.getKey()))
          .responseField(getResponseObject(openApi, mediaType))
          .isDefault(isDefault(mediaType.getExtensions()))
          .templateName(getTemplateName(mediaType.getExtensions()))
          .build();
    };
  }

  private Map<String, ResponseHeader> createResponseHeaders(Map<String, Header> headers, String queryName) {
    if (Objects.isNull(headers)) {
      return Collections.emptyMap();
    }

    return headers.entrySet()
        .stream()
        .map(headerEntry -> mapHeader(headerEntry, queryName))
        .collect(Collectors.toMap(ResponseHeader::getName, Function.identity()));
  }

  private ResponseHeader mapHeader(Map.Entry<String, Header> headerEntry, String queryName) {
    Schema<?> schema = Objects.nonNull(headerEntry.getValue()
        .get$ref()) ? resolveSchema(openApi,
            headerEntry.getValue()
                .getSchema(),
            headerEntry.getValue()
                .get$ref())
            : headerEntry.getValue()
                .getSchema();

    if (Objects.isNull(schema.getExtensions()) || Objects.isNull(schema.getExtensions()
        .get(X_DWS_EXPR))) {
      throw invalidConfigurationException(
          "Found invalid schema for response header '{}' of query '{}': x-dws-expr is missing or null",
          headerEntry.getKey(), queryName);
    }

    return ResponseHeader.builder()
        .name(headerEntry.getKey())
        .defaultValue((String) schema.getDefault())
        .type(schema.getType())
        .dwsExpressionMap(getDwsExpression(schema))
        .build();
  }

  private String getTemplateName(Map<String, Object> extensions) {
    String templateName = null;
    if (extensions != null) {
      templateName = (String) extensions.get(TEMPLATE_NAME_VENDOR_EXTENSION);
    }
    return templateName;
  }

  private boolean isDefault(Map<String, Object> extensions) {
    Boolean result = false;

    if (extensions != null) {
      result = (Boolean) extensions.get(DEFAULT_CONTENT_TYPE_VENDOR_EXTENSION);
    }
    return result != null ? result : false;
  }

  private OasField getResponseObject(OpenAPI openApi, MediaType content) {
    OasFieldBuilder builder = new OasFieldBuilder(openApi);
    OasField responseObject = null;
    if (Objects.nonNull(content.getSchema())) {
      String ref = content.getSchema()
          .get$ref();

      Schema<?> schema = Objects.nonNull(ref) ? resolveSchema(openApi, content.getSchema()) : content.getSchema();
      responseObject = builder.build(schema);
    }
    return responseObject;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getDwsExpression(Schema<?> schema) {
    Object result = getDwsExtension(schema, X_DWS_EXPR);

    if (Objects.isNull(result)) {
      return null;
    }

    if (Objects.equals(OasConstants.OBJECT_TYPE, schema.getType())) {
      throw invalidConfigurationException("Extension '{}' is not allowed for `object` type", X_DWS_EXPR);
    }

    if (result instanceof String) {
      return Map.of(X_DWS_EXPR_VALUE, result.toString());
    }

    if (!(result instanceof Map) || !((Map) result).containsKey(X_DWS_EXPR_VALUE)) {
      throw invalidConfigurationException("Extension '{}' should contain a key named '{}'.", X_DWS_EXPR,
          X_DWS_EXPR_VALUE);
    }

    return (Map) result;
  }

  protected static Optional<String> getXdwsType(Schema<?> schema) {
    if (Objects.nonNull(schema.getExtensions())) {
      Object type = schema.getExtensions()
          .get(X_DWS_TYPE);
      return Optional.ofNullable((String) type);
    }
    return Optional.empty();
  }
}
