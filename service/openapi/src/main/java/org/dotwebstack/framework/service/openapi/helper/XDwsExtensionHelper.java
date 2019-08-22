package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import java.util.Objects;

public class XDwsExtensionHelper {

  private XDwsExtensionHelper() {}

  public static boolean supportsDwsType(Parameter parameter, String typeString) {
    Map<String, Object> extensions = parameter.getExtensions();
    return supportsDwsType(typeString, extensions);
  }

  public static boolean supportsDwsType(RequestBody requestBody, String typeString) {
    Map<String, Object> extensions = requestBody.getExtensions();
    return supportsDwsType(typeString, extensions);
  }

  private static boolean supportsDwsType(String typeString, Map<String, Object> extensions) {
    if (Objects.nonNull(extensions)) {
      String handler = (String) extensions.get(X_DWS_TYPE);
      if (Objects.nonNull(handler)) {
        return Objects.equals(handler, typeString);
      }
    }
    return false;
  }

}
