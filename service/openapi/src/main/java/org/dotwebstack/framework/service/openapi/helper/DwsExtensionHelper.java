package org.dotwebstack.framework.service.openapi.helper;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_DEFAULT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_FIELD;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETERS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_VALUEEXPR;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TRANSIENT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.jexl.JexlExpression;

public class DwsExtensionHelper {

  static final String DWS_QUERY_JEXL_CONTEXT_REQUEST = "request";

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

  public static boolean isDefaultMediaType(@NonNull MediaType mediaType) {
    Map<String, Object> extensions = mediaType.getExtensions();
    return extensions != null && Boolean.TRUE.equals(extensions.get(X_DWS_DEFAULT));
  }

  public static int defaultMediaTypeFirst(@NonNull MediaType mediaTypeOne, @NonNull MediaType mediaTypeTwo) {
    var oneDefault = isDefaultMediaType(mediaTypeOne);
    var twoDefault = isDefaultMediaType(mediaTypeTwo);

    if (oneDefault && !twoDefault) {
      return -1;
    } else if (!oneDefault && twoDefault) {
      return 1;
    } else {
      return 0;
    }
  }

  public static boolean isTransient(@NonNull Schema<?> schema) {
    Boolean isEnvelope = (Boolean) getDwsExtension(schema, X_DWS_ENVELOPE);
    Boolean isTransient = (Boolean) getDwsExtension(schema, X_DWS_TRANSIENT);
    return isTrue(isEnvelope) || isTrue(isTransient) || isExpr(schema) || isDefault(schema);
  }

  public static Optional<String> getDwsQueryName(@NonNull Operation operation) {
    if (operation.getExtensions() == null || !operation.getExtensions()
        .containsKey(X_DWS_QUERY)) {
      return Optional.empty();
    }
    Object dwsQueryName = operation.getExtensions()
        .get(X_DWS_QUERY);
    if (dwsQueryName instanceof Map) {
      return Optional.of((String) ((Map<?, ?>) dwsQueryName).get(X_DWS_QUERY_FIELD));
    }
    return Optional.of((String) dwsQueryName);
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
          Objects.requireNonNullElse((List<?>) ((Map<?, ?>) dwsQuery).get(X_DWS_QUERY_PARAMETERS), emptyList());
      dwsParameters.forEach(o -> result.put((String) ((Map<?, ?>) o).get(X_DWS_QUERY_PARAMETER_NAME),
          (String) ((Map<?, ?>) o).get(X_DWS_QUERY_PARAMETER_VALUEEXPR)));
    }
    return result;
  }

  public static Optional<JexlExpression> getJexlExpression(@NonNull Schema<?> schema) {
    var expr = getDwsExtension(schema, X_DWS_EXPR);
    if (expr == null) {
      return Optional.empty();
    }

    return getJexlExpression(expr, schema, val -> val);
  }

  @SuppressWarnings("unchecked")
  public static Optional<JexlExpression> getJexlExpression(@NonNull Object expr, @NonNull Object context,
      UnaryOperator<String> expressionValueAdapter) {
    if (expr instanceof String) {
      var value = expressionValueAdapter.apply(String.valueOf(expr));

      return Optional.of(JexlExpression.builder()
          .value(value)
          .build());
    } else if (expr instanceof Map<?, ?>) {
      var exprMap = (Map<String, Object>) expr;

      if (exprMap.containsKey(X_DWS_EXPR_VALUE)) {
        var exprBuilder = JexlExpression.builder();
        var value = checkAndGetExpressionStringProperty(exprMap.get(X_DWS_EXPR_VALUE), X_DWS_EXPR_VALUE, context);

        exprBuilder.value(expressionValueAdapter.apply(value));

        if (exprMap.containsKey(X_DWS_EXPR_FALLBACK_VALUE)) {
          var fallback = checkAndGetExpressionStringProperty(exprMap.get(X_DWS_EXPR_FALLBACK_VALUE),
              X_DWS_EXPR_FALLBACK_VALUE, context);

          exprBuilder.fallback(fallback);
        }

        return Optional.of(exprBuilder.build());
      }
    }

    throw invalidConfigurationException("Unsupported value {} for {} found in {}", expr, X_DWS_EXPR, context);
  }

  private static String checkAndGetExpressionStringProperty(Object value, String propertyName, Object context) {
    if (value == null) {
      return null;
    }

    if (!(value instanceof String)) {
      throw invalidConfigurationException("Expected value of type 'string', but found {} for {}.{} found in {}", value,
          X_DWS_EXPR, propertyName, context);
    }

    return (String) value;
  }
}
