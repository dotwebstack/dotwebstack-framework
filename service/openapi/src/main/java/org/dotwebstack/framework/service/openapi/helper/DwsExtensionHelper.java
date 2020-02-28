package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_FIELD;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETERS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_VALUEEXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_REQUIRED_FIELDS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;

public class DwsExtensionHelper {

  public static final String DWS_QUERY_JEXL_CONTEXT_REQUEST = "request";

  public static final String DWS_QUERY_JEXL_CONTEXT_PARAMS = "params";

  private DwsExtensionHelper() {}

  public static String getDwsType(@NonNull Schema<?> schema) {
    return (String) getDwsExtension(schema, X_DWS_TYPE);
  }

  public static boolean supportsDwsType(@NonNull Parameter parameter, @NonNull String typeString) {
    Map<String, Object> extensions = parameter.getExtensions();
    return Objects.nonNull(extensions) && supportsDwsType(typeString, extensions);
  }

  public static boolean supportsDwsType(@NonNull RequestBody requestBody, @NonNull String typeString) {
    Map<String, Object> extensions = requestBody.getExtensions();
    return Objects.nonNull(extensions) && supportsDwsType(typeString, extensions);
  }

  private static boolean supportsDwsType(String typeString, Map<String, Object> extensions) {
    String handler = (String) extensions.get(X_DWS_TYPE);
    return (Objects.nonNull(handler)) && Objects.equals(handler, typeString);
  }

  public static boolean hasDwsExtensionWithValue(@NonNull Parameter parameter, @NonNull String typeName,
      @NonNull Object value) {
    Map<String, Object> extensions = parameter.getExtensions();
    return (Objects.nonNull(extensions)) && Objects.equals(value, extensions.get(typeName));
  }

  public static Object getDwsExtension(@NonNull Schema<?> schema, @NonNull String typeName) {
    Map<String, Object> extensions = schema.getExtensions();
    return (Objects.nonNull(extensions)) ? extensions.get(typeName) : null;
  }

  public static boolean isExpr(@NonNull Schema<?> schema) {
    return Objects.nonNull(getDwsExtension(schema, X_DWS_EXPR));
  }

  public static boolean isEnvelope(@NonNull Schema<?> schema) {
    Boolean isEnvelope = (Boolean) getDwsExtension(schema, X_DWS_ENVELOPE);
    return (Objects.nonNull(isEnvelope) && isEnvelope) || isExpr(schema);
  }

  public static String getDwsQueryName(@NonNull Operation operation) {
    Object dwsQueryName = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQueryName instanceof Map) {
      return (String) ((Map) dwsQueryName).get(X_DWS_QUERY_FIELD);
    }
    return (String) dwsQueryName;
  }

  @SuppressWarnings("unchecked")
  public static List<String> getDwsRequiredFields(@NonNull Operation operation) {
    Object dwsQuery = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQuery instanceof Map) {
      return (List<String>) ((Map) dwsQuery).get(X_DWS_QUERY_REQUIRED_FIELDS);
    }
    return Collections.emptyList();
  }

  public static Map<String, String> getDwsQueryParameters(@NonNull Operation operation) {
    Map<String, String> result = new HashMap<>();
    Object dwsQuery = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQuery instanceof Map) {
      List<?> dwsParameters =
          Objects.requireNonNullElse((List<?>) ((Map) dwsQuery).get(X_DWS_QUERY_PARAMETERS), Collections.emptyList());
      dwsParameters.stream()
          .forEach(o -> result.put((String) ((Map) o).get(X_DWS_QUERY_PARAMETER_NAME),
              (String) ((Map) o).get(X_DWS_QUERY_PARAMETER_VALUEEXPR)));
    }
    return result;
  }
}
