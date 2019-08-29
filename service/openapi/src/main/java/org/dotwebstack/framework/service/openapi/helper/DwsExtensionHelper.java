package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TEMPLATE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;

public class DwsExtensionHelper {

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

  public static boolean isTemplate(@NonNull Schema<?> schema) {
    String isTemplate = ((String) getDwsExtension(schema, X_DWS_TEMPLATE));
    return Objects.nonNull(isTemplate);
  }

  public static boolean isEnvelope(@NonNull Schema<?> schema) {
    Boolean isEnvelope = (Boolean) getDwsExtension(schema, X_DWS_ENVELOPE);
    return Objects.nonNull(isEnvelope) ? isEnvelope : false;
  }
}
