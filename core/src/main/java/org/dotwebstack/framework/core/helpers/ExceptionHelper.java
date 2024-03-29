package org.dotwebstack.framework.core.helpers;

import graphql.ExecutionInput;
import java.util.MissingFormatArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.RequestValidationException;

@Slf4j
public class ExceptionHelper {

  public static final String UNSUPPORTED_TYPE_ERROR_TEXT = "unsupported type: '{}'";

  public static final String DEPRECATED_METHOD_ERROR_TEXT =
      "This method signature is deprecated and should not be used.";

  protected ExceptionHelper() {}

  public static String formatMessage(String message, Object... arguments) {
    try {
      return message != null ? String.format(message.replaceAll("\\{\\}", "%s"), arguments) : "";
    } catch (MissingFormatArgumentException exception) {
      throw new DotWebStackRuntimeException("Missing container for exception with message '{}'", message);
    }
  }

  public static Throwable findCause(Object... arguments) {
    if (arguments != null && arguments.length > 0) {
      Object lastArgument = arguments[arguments.length - 1];
      if (lastArgument instanceof Throwable throwable) {
        return throwable;
      }
    }
    return null;
  }

  public static Object[] joinArguments(Throwable cause, Object[] arguments) {
    var newArguments = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
    newArguments[newArguments.length - 1] = cause;

    return newArguments;
  }

  public static IllegalStateException illegalStateException(String message, Object... arguments) {
    return new IllegalStateException(formatMessage(message, arguments), findCause(arguments));
  }

  public static IllegalArgumentException illegalArgumentException(String message, Object... arguments) {
    return new IllegalArgumentException(formatMessage(message, arguments), findCause(arguments));
  }

  public static UnsupportedOperationException unsupportedOperationException(String message, Object... arguments) {
    return new UnsupportedOperationException(formatMessage(message, arguments), findCause(arguments));
  }

  public static InvalidConfigurationException invalidConfigurationException(String message, Object... arguments) {
    return new InvalidConfigurationException(formatMessage(message, arguments), findCause(arguments));
  }

  public static InternalServerErrorException internalServerErrorException(ExecutionInput executionInput) {
    LOG.error("Internal server error occurred for executionInput: {}", executionInput);

    return new InternalServerErrorException("An internal server error has occurred!");
  }

  public static RequestValidationException requestValidationException(String message, Object... arguments) {
    return new RequestValidationException(formatMessage(message, arguments), findCause(arguments));
  }

}
