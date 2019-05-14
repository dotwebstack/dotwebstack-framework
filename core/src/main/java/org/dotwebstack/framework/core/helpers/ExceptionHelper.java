package org.dotwebstack.framework.core.helpers;

public class ExceptionHelper {

  private ExceptionHelper() { }

  public static String formatMessage(String message, Object... arguments) {
    return message != null ? String.format(
        message.replaceAll("\\{\\}", "%s"), arguments) : "";
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

}
