package org.dotwebstack.framework.core.helpers;

import java.util.MissingFormatArgumentException;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;

public class ExceptionHelper {

  private ExceptionHelper() { }

  public static String formatMessage(String message, Object... arguments) {
    try {
      return message != null ? String.format(
          message.replaceAll("\\{\\}", "%s"), arguments) : "";
    } catch (MissingFormatArgumentException exception) {
      throw new DotWebStackRuntimeException("Missing argument for exception with message '{}'",message);
    }
  }

  public static Throwable findCause(Object... arguments) {
    if (arguments != null && arguments.length > 0) {
      Object lastArgument = arguments[arguments.length - 1];
      if (lastArgument instanceof Throwable) {
        return (Throwable) lastArgument;
      }
    }
    return null;
  }

  public static Object[] joinArguments(Throwable cause, Object[] arguments) {
    Object[] newArguments = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
    newArguments[newArguments.length - 1] = cause;

    return newArguments;
  }

  public static IllegalArgumentException illegalArgumentException(
      String message, Object... arguments) {
    return new IllegalArgumentException(formatMessage(message, arguments), findCause(arguments));
  }

  public static UnsupportedOperationException unsupportedOperationException(String message, Object... arguments) {
    return new UnsupportedOperationException(formatMessage(message,arguments), findCause(arguments));
  }
}
