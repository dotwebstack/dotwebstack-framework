package org.dotwebstack.framework.service.openapi.exception;

import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.mapping.MappingException;

public class OpenApiExceptionHelper extends ExceptionHelper {

  private OpenApiExceptionHelper() {}

  public static MappingException mappingException(String message, Object... arguments) {
    return new MappingException(formatMessage(message, arguments), findCause(arguments));
  }

  public static ParameterValidationException parameterValidationException(String message, Object... arguments) {
    return new ParameterValidationException(formatMessage(message, arguments), findCause(arguments));
  }

  public static NoResultFoundException noResultFoundException(String message, Object... arguments) {
    return new NoResultFoundException(formatMessage(message, arguments), findCause(arguments));
  }

  public static GraphQlErrorException graphQlErrorException(String message, Object... arguments) {
    return new GraphQlErrorException(formatMessage(message, arguments), findCause(arguments));
  }
}
