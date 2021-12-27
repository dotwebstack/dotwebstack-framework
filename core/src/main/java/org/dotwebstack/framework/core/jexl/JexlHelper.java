package org.dotwebstack.framework.core.jexl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
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

@Slf4j
public class JexlHelper {

  private static final String ENVIRONMENT_PREFIX = "env.";

  private static final String REQUEST_PREFIX = "request";

  private static final String ARGUMENT_PREFIX = "args";

  private static final String DATA_PREFIX = "data";

  private final JexlEngine engine;

  public JexlHelper(@NonNull JexlEngine engine) {
    this.engine = engine;
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Object serverRequest,
      Map<String, Object> argParams) {
    return getJexlContext(envParams, serverRequest, argParams, null);
  }

  public static JexlContext getJexlContext(Map<String, String> envParams, Object serverRequest,
      Map<String, Object> argParams, Object resultData) {
    JexlContext jexlContext = new MapContext();

    if (envParams != null) {
      envParams.forEach((key, value) -> jexlContext.set(ENVIRONMENT_PREFIX + key, value));
    }

    if (serverRequest != null) {
      jexlContext.set(REQUEST_PREFIX, serverRequest);
    }

    if (argParams != null) {
      jexlContext.set(ARGUMENT_PREFIX, argParams);
    }

    if (resultData != null) {
      jexlContext.set(DATA_PREFIX, resultData);
    }

    return jexlContext;
  }

  public static JexlContext getJexlContext(GraphQLFieldDefinition graphQlField) {
    JexlContext jexlContext = new MapContext();

    if (Objects.nonNull(graphQlField)) {
      graphQlField.getArguments()
          .stream()
          .filter(argument -> argument.getArgumentValue()
              .isSet()
              || argument.getArgumentDefaultValue()
                  .isSet())
          .forEach(argument -> Optional.ofNullable(argument.getArgumentValue()
              .getValue())
              .or(() -> Optional.ofNullable(argument.getArgumentDefaultValue()
                  .getValue()))
              .ifPresent(argValue -> jexlContext.set(ARGUMENT_PREFIX + "." + argument.getName(), argValue)));
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
      if (Objects.nonNull(fallbackString)) {
        try {
          LOG.debug("Executing fallback script following an error in the original script: {}",
              getJexlExceptionMessage(exception));
          return evaluateScript(fallbackString, context, clazz);
        } catch (Exception fallbackException) {
          LOG.warn("Something went wrong while executing the fallback script: {}",
              getJexlExceptionMessage(fallbackException));
        }
      } else {
        LOG.warn("Something went wrong while executing the script with no fallback: {}",
            getJexlExceptionMessage(exception));
      }
      return Optional.empty();
    }
  }

  private String getJexlExceptionMessage(Exception exception) {
    if (exception.getCause() == null) {
      return exception.getMessage();
    }

    return String.format("%s : %s", exception.getMessage(), exception.getCause());
  }

  public <T> Optional<T> evaluateScript(@NonNull String scriptString, @NonNull JexlContext context,
      @NonNull Class<T> clazz) {
    Object evaluated;
    try {
      JexlScript script = this.engine.createScript(scriptString);
      evaluated = script.execute(context);
    } catch (Exception exception) {
      throw invalidConfigurationException("Error evaluating expression {}", scriptString, exception);
    }
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
    var expressionString = DirectiveUtils.getArgument(directive, argumentName, String.class);
    if (expressionString == null) {
      return Optional.empty();
    } else {
      return evaluateExpression(expressionString, context, clazz);
    }
  }

}
