package org.dotwebstack.framework.service.openapi.helper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_DEFAULT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_OPERATION;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_FIELD;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETERS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_VALUEEXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_REQUIRED_FIELDS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TRANSIENT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.GraphQlBinding;

public class DwsExtensionHelper {

  static final String DWS_QUERY_JEXL_CONTEXT_REQUEST = "request";

  static final String DWS_QUERY_JEXL_CONTEXT_PARAMS = "params";

  private DwsExtensionHelper() {}

  public static String getDwsType(@NonNull Schema<?> schema) {
    return (String) getDwsExtension(schema, X_DWS_TYPE);
  }

  public static boolean supportsDwsType(@NonNull Parameter parameter, @NonNull String typeString) {
    Map<String, Object> extensions = parameter.getExtensions();
    return extensions != null && supportsDwsType(typeString, extensions);
  }

  public static boolean supportsDwsType(@NonNull RequestBody requestBody, @NonNull String typeString) {
    Map<String, Object> extensions = requestBody.getExtensions();
    return extensions != null && supportsDwsType(typeString, extensions);
  }

  private static boolean supportsDwsType(String typeString, Map<String, Object> extensions) {
    String handler = (String) extensions.get(X_DWS_TYPE);
    return (handler != null) && handler.equals(typeString);
  }

  public static boolean hasDwsExtensionWithValue(@NonNull Parameter parameter, @NonNull String typeName,
      @NonNull Object value) {
    Map<String, Object> extensions = parameter.getExtensions();
    return (extensions != null) && value.equals(extensions.get(typeName));
  }

  public static Object getDwsExtension(@NonNull Schema<?> schema, @NonNull String typeName) {
    Map<String, Object> extensions = schema.getExtensions();
    return (extensions != null) ? extensions.get(typeName) : null;
  }

  private static boolean isExpr(@NonNull Schema<?> schema) {
    return getDwsExtension(schema, X_DWS_EXPR) != null;
  }

  public static boolean isDefault(@NonNull Schema<?> schema) {
    return getDwsExtension(schema, X_DWS_DEFAULT) != null;
  }

  public static boolean isTransient(@NonNull Schema<?> schema) {
    Boolean isEnvelope = (Boolean) getDwsExtension(schema, X_DWS_ENVELOPE);
    Boolean isTransient = (Boolean) getDwsExtension(schema, X_DWS_TRANSIENT);
    return isTrue(isEnvelope) || isTrue(isTransient) || isExpr(schema) || isDefault(schema);
  }

  public static boolean isDwsOperation(@NonNull Operation operation) {
    if (operation.getExtensions() == null || !operation.getExtensions()
        .containsKey(X_DWS_OPERATION)) {
      return true;
    }
    Object operationValue = operation.getExtensions()
        .get(X_DWS_OPERATION);

    if (operationValue instanceof Boolean) {
      return (boolean) operationValue;
    }
    return true;
  }

  public static Optional<String> getDwsQueryName(@NonNull Operation operation) {
    if (operation.getExtensions() == null || !operation.getExtensions()
        .containsKey(X_DWS_QUERY)) {
      return Optional.empty();
    }
    Object dwsQueryName = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQueryName instanceof Map) {
      return Optional.of((String) ((Map) dwsQueryName).get(X_DWS_QUERY_FIELD));
    }
    return Optional.of((String) dwsQueryName);
  }

  public static Optional<GraphQlBinding> getGraphQlBinding(@NonNull Operation operation) {
    if (operation.getExtensions() == null || !operation.getExtensions()
        .containsKey(X_DWS_QUERY)) {
      return Optional.empty();
    }
    Object dwsQueryName = operation.getExtensions()
        .get(X_DWS_QUERY);
    GraphQlBinding.GraphQlBindingBuilder builder = GraphQlBinding.builder();
    if (dwsQueryName instanceof Map) {
      Map<String, Object> bindingMap = (Map<String, Object>) dwsQueryName;
      builder.queryName(bindingMap.get(X_DWS_QUERY_FIELD).toString());
      String selector = (String) bindingMap.get("selector");
      builder.selector(selector);
    }
    else {
      builder.queryName(dwsQueryName.toString());
    }

    return Optional.of(builder.build());
  }

  @SuppressWarnings("unchecked")
  public static List<String> getDwsRequiredFields(@NonNull Operation operation) {
    if (operation.getExtensions() == null) {
      return emptyList();
    }

    Object dwsQuery = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQuery instanceof Map) {
      return (List<String>) ((Map) dwsQuery).get(X_DWS_QUERY_REQUIRED_FIELDS);
    }
    return emptyList();
  }

  public static Map<String, String> getDwsQueryParameters(@NonNull Operation operation) {
    if (operation.getExtensions() == null) {
      return emptyMap();
    }

    Map<String, String> result = new HashMap<>();
    Object dwsQuery = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQuery instanceof Map) {
      List<?> dwsParameters =
          Objects.requireNonNullElse((List<?>) ((Map) dwsQuery).get(X_DWS_QUERY_PARAMETERS), emptyList());
      dwsParameters.forEach(o -> result.put((String) ((Map) o).get(X_DWS_QUERY_PARAMETER_NAME),
          (String) ((Map) o).get(X_DWS_QUERY_PARAMETER_VALUEEXPR)));
    }
    return result;
  }
}
