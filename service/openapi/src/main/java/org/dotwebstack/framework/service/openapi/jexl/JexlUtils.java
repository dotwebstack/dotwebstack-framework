package org.dotwebstack.framework.service.openapi.jexl;

import java.util.Optional;
import org.apache.commons.jexl3.JexlContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;

public class JexlUtils {

  private JexlUtils() {}

  public static <T> Optional<T> evaluateJexlExpression(JexlExpression jexlExpression, JexlHelper jexlHelper,
      JexlContext jexlContext, Class<T> clazz) {
    return jexlExpression.getFallback()
        .map(fallback -> jexlHelper.evaluateScriptWithFallback(jexlExpression.getValue(), fallback, jexlContext, clazz))
        .orElseGet(() -> jexlHelper.evaluateScript(jexlExpression.getValue(), jexlContext, clazz));
  }
}
