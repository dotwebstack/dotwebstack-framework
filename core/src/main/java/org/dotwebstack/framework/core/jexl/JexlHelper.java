package org.dotwebstack.framework.core.jexl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.cast;

import graphql.schema.GraphQLDirective;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.helpers.GraphQlValueHelper;
import org.dotwebstack.framework.core.query.GraphQlField;

public class JexlHelper {

  private static final String ENVIRONMENT_PREFIX = "env.";

  private static final String ARGUMENT_PREFIX = "args.";

  private final JexlEngine engine;

  public JexlHelper(@NonNull JexlEngine engine) {
    this.engine = engine;
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Map<String, Object> argParams) {
    return getJexlContext(envParams, argParams, null);
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Map<String, Object> argParams, GraphQlField graphQlField) {
    JexlContext jexlContext = new MapContext();

    if (Objects.nonNull(envParams)) {
      envParams.forEach((key, value) -> jexlContext.set(ENVIRONMENT_PREFIX + key, value));
    }

    if (Objects.nonNull(graphQlField)) {
      graphQlField.getArguments()
          .stream()
          .filter(argument -> Objects.nonNull(argument.getDefaultValue()))
          .forEach(argument -> jexlContext.set(ARGUMENT_PREFIX + argument.getName(),
              GraphQlValueHelper.getStringValue(argument.getDefaultValue())));
    }

    if (Objects.nonNull(argParams)) {
      argParams.forEach((key, value) -> jexlContext.set(ARGUMENT_PREFIX + key, value.toString()));
    }

    return jexlContext;
  }

  public <T> Optional<T> evaluateScript(String scriptString, JexlContext context, Class<T> clazz) {
    JexlScript script = this.engine.createScript(scriptString);
    Object evaluated = script.execute(context);
    if (evaluated == null) {
      return Optional.empty();
    } else if (!clazz.isInstance(evaluated)) {
      throw illegalArgumentException("Jexl evaluateDirectiveArgument type mismatch: expected[{}], but was [{}].", clazz,
          evaluated.getClass());
    } else {
      return Optional.of(cast(clazz, evaluated));
    }
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

  public <T> Optional<T> evaluateDirectiveArgument(GraphQLDirective directive, String argumentName, JexlContext context,
      Class<T> clazz) {
    String expressionString = DirectiveUtils.getArgument(directive, argumentName, String.class);
    if (expressionString == null) {
      return Optional.empty();
    } else {
      return evaluateExpression(expressionString, context, clazz);
    }
  }
}
