package org.dotwebstack.framework.service.openapi.exception;

import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class OpenApiExceptionHelper extends ExceptionHelper {

  private OpenApiExceptionHelper() {}

  public static ParameterValidationException parameterValidationException(String message, Object... arguments) {
    return new ParameterValidationException(formatMessage(message, arguments), findCause(arguments));
  }

  public static NoResultFoundException noResultFoundException(String message, Object... arguments) {
    return new NoResultFoundException(formatMessage(message, arguments), findCause(arguments));
  }


}
