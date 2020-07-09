package org.dotwebstack.framework.core.jexl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.cast;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.helpers.GraphQlValueHelper;
import org.dotwebstack.framework.core.query.GraphQlField;

@Slf4j
public class JexlHelper {

  private static final String ENVIRONMENT_PREFIX = "env.";

  public static final String ARGUMENT_PREFIX = "args.";

  private static final String FIELDS_PREFIX = "fields.";

  private final JexlEngine engine;

  public JexlHelper(@NonNull JexlEngine engine) {
    this.engine = engine;
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Map<String, Object> argParams) {
    return getJexlContext(envParams, argParams, null, null);
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Map<String, Object> argParams,
      GraphQlField graphQlField, Map<String, Object> resultData) {
    JexlContext jexlContext = new MapContext();

    if (envParams != null) {
      envParams.forEach((key, value) -> jexlContext.set(ENVIRONMENT_PREFIX + key, value));
    }

    if (graphQlField != null) {
      graphQlField.getArguments()
          .stream()
          .filter(argument -> Objects.nonNull(argument.getDefaultValue()))
          .forEach(argument -> jexlContext.set(ARGUMENT_PREFIX + argument.getName(),
              GraphQlValueHelper.getValue(argument.getType(), argument.getDefaultValue())));
    }

    if (resultData != null) {
      resultData.entrySet()
          .stream()
          .filter(entry -> !(entry.getValue() instanceof Map))
          .forEach(entry -> jexlContext.set(FIELDS_PREFIX + entry.getKey(), entry.getValue()));
    }

    if (argParams != null) {
      argParams.forEach((key, value) -> jexlContext.set(ARGUMENT_PREFIX + key, value.toString()));
    }

    return jexlContext;
  }

  public static JexlContext getJexlContext(GraphQLFieldDefinition graphQlField) {
    JexlContext jexlContext = new MapContext();

    if (Objects.nonNull(graphQlField)) {
      graphQlField.getArguments()
          .stream()
          .filter(argument -> argument.getValue() != null || argument.getDefaultValue() != null)
          .forEach(argument -> jexlContext.set(ARGUMENT_PREFIX + argument.getName(),
              argument.getValue() != null ? argument.getValue() : argument.getDefaultValue()));
    }

    return jexlContext;
  }

  public static void updateContext(@NonNull JexlContext context, @NonNull Map<String, Object> requestArguments) {
    requestArguments.forEach((key, value) -> context.set(ARGUMENT_PREFIX + key, value));
  }

  public <T> Optional<T> evaluateScriptWithFallback(@NonNull String scriptString, String fallbackString,
      @NonNull JexlContext context, @NonNull Class<T> clazz) {
    try {
      return evaluateScript(scriptString, context, clazz);
    } catch (Exception exception) {
      LOG.warn("Something went wrong while executing the original script: " + exception.getMessage());
      if (Objects.nonNull(fallbackString)) {
        try {
          LOG.warn("Executing fallback script");
          return evaluateScript(fallbackString, context, clazz);
        } catch (Exception fallbackException) {
          LOG.warn("Something went wrong while executing the fallback script: " + fallbackException.getMessage());
        }
      }
      return Optional.empty();
    }
  }

  public <T> Optional<T> evaluateScript(@NonNull String scriptString, @NonNull JexlContext context,
      @NonNull Class<T> clazz) {
    JexlScript script = this.engine.createScript(scriptString);
    Object evaluated = script.execute(context);
    if (Objects.isNull(evaluated)) {
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
