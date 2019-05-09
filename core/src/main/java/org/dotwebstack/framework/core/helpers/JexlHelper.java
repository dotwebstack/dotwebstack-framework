package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.cast;

import graphql.schema.GraphQLDirective;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.dotwebstack.framework.core.directives.DirectiveUtils;

@RequiredArgsConstructor
public class JexlHelper {

  final JexlEngine engine;

  public <T> Optional<T> evaluateExpression(final String expressionString,
                                            final JexlContext context,
                                            final Class<T> clazz) {
    final JexlExpression expression = this.engine.createExpression(expressionString);
    final Object evaluated = expression.evaluate(context);
    if (evaluated == null) {
      return Optional.empty();
    } else if (!clazz.isInstance(evaluated)) {
      throw new IllegalArgumentException(
          String.format("Jexl evaluateDirectiveArgument type mismatch: expected[%s], but was [%s].",
              clazz, evaluated.getClass()));
    } else {
      return Optional.of(cast(clazz,evaluated));
    }
  }

  public <T> Optional<T> evaluateDirectiveArgument(final String argumentName,
                                                   final GraphQLDirective directive,
                                                   final JexlContext context,
                                                   final Class<T> clazz) {
    final String expressionString = DirectiveUtils.getArgument(argumentName, directive,
        String.class);
    if (expressionString == null) {
      return Optional.empty();
    } else {
      return evaluateExpression(expressionString, context, clazz);
    }
  }
}
