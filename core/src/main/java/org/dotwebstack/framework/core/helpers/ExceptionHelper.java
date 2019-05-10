package org.dotwebstack.framework.core.helpers;

public class ExceptionHelper {

  public static String formatMessage(final String message,
                                     final Object... arguments) {
    if (message != null) {
      return String.format(message.replaceAll("\\{\\}", "%s"), arguments);
    }
    return "";
  }

  public static Throwable findCause(final Object... arguments) {
    if (arguments != null && arguments.length > 0) {
      final Object lastArgument = arguments[arguments.length - 1];
      if (lastArgument instanceof Throwable) {
        return (Throwable) lastArgument;
      }
    }
    return null;
  }

  public static Object[] joinArguments(final Throwable cause,
                                       final Object[] arguments) {
    final Object[] newArguments = new Object[arguments.length + 1];
    System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
    newArguments[newArguments.length - 1] = cause;

    return newArguments;
  }

  public static IllegalArgumentException illegalArgumentException(final String message,
                                                                  final Object... arguments) {
    return new IllegalArgumentException(formatMessage(message, arguments), findCause(arguments));
  }

}
