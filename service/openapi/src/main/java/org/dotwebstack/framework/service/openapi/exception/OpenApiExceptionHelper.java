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

  public static NoContentException noContentException(String message, Object... arguments) {
    return new NoContentException(formatMessage(message, arguments), findCause(arguments));
  }

  public static NotFoundException notFoundException(String message, Object... arguments) {
    return new NotFoundException(formatMessage(message, arguments), findCause(arguments));
  }

  public static GraphQlErrorException graphQlErrorException(String message, Object... arguments) {
    return new GraphQlErrorException(formatMessage(message, arguments), findCause(arguments));
  }

  public static BadRequestException badRequestException(String message, Object... arguments) {
    return new BadRequestException(formatMessage(message, arguments), findCause(arguments));
  }

  public static NotAcceptableException notAcceptableException(String message, Object... arguments) {
    return new NotAcceptableException(formatMessage(message, arguments), findCause(arguments));
  }

  public static InvalidOpenApiConfigurationException invalidOpenApiConfigurationException(String message,
      Object... arguments) {
    return new InvalidOpenApiConfigurationException(formatMessage(message, arguments), findCause(arguments));
  }

  public static IllegalArgumentException illegalArgumentException(String message, Object... arguments) {
    return new IllegalArgumentException(formatMessage(message, arguments), findCause(arguments));
  }
}
