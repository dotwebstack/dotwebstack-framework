package org.dotwebstack.framework.service.openapi.jexl;

import java.util.Map;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;

public class JexlContextUtils {

  private JexlContextUtils() {}

  public static JexlContext createJexlContext(EnvironmentProperties environmentProperties,
      Map<String, Object> operationParameters) {
    return createJexlContext(environmentProperties, operationParameters, null);
  }

  public static JexlContext createJexlContext(EnvironmentProperties environmentProperties,
      Map<String, Object> operationParameters, Object data) {
    var context = new MapContext();

    context.set("env", environmentProperties.getAllProperties());
    context.set("args", operationParameters);

    if (data != null) {
      context.set("data", data);
    }

    return context;
  }
}
