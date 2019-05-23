package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.cast;

import graphql.schema.GraphQLDirective;
import java.util.Optional;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.dotwebstack.framework.core.directives.DirectiveUtils;

public class JexlHelper {

  private final JexlEngine engine;

  public JexlHelper(JexlEngine engine) {
    this.engine = engine;
  }

  public <T> Optional<T> evaluateExpression(String expressionString, JexlContext context, Class<T> clazz) {
    JexlExpression expression = this.engine.createExpression(expressionString);
    Object evaluated = expression.evaluate(context);
    if (evaluated == null) {
      return Optional.empty();
    } else if (!clazz.isInstance(evaluated)) {
      throw illegalArgumentException("Jexl evaluateDirectiveArgument type mismatch: expected[{}], but was [{}].", clazz,
          evaluated.getClass());
    } else {
      return Optional.of(cast(clazz, evaluated));
    }
  }

  public <T> Optional<T> evaluateDirectiveArgument(String argumentName, GraphQLDirective directive, JexlContext context,
      Class<T> clazz) {
    String expressionString = DirectiveUtils.getArgument(argumentName, directive, String.class);
    if (expressionString == null) {
      return Optional.empty();
    } else {
      return evaluateExpression(expressionString, context, clazz);
    }
  }
}
