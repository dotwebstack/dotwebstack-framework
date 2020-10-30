package org.dotwebstack.framework.service.openapi.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.templating.TemplatingException;
import org.dotwebstack.framework.service.openapi.mapping.MappingException;

public class ExceptionRuleHelper {
  public static final List<ExceptionRule> MAPPING = List.of(ExceptionRule.builder()
      .exception(NotAcceptableException.class)
      .responseStatus(NOT_ACCEPTABLE)
      .title("Unsupported media type requested")
      .build(),
      ExceptionRule.builder()
          .exception(ParameterValidationException.class)
          .responseStatus(BAD_REQUEST)
          .title("Error while obtaining request parameters")
          .detail(true)
          .build(),
      ExceptionRule.builder()
          .exception(MappingException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title("Internal server error")
          .build(),
      ExceptionRule.builder()
          .exception(GraphQlErrorException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title("Internal server error")
          .build(),
      ExceptionRule.builder()
          .exception(NoContentException.class)
          .responseStatus(NO_CONTENT)
          .title("No content")
          .build(),
      ExceptionRule.builder()
          .exception(NotFoundException.class)
          .responseStatus(NOT_FOUND)
          .title("No results found")
          .build(),
      ExceptionRule.builder()
          .exception(UnsupportedOperationException.class)
          .responseStatus(UNSUPPORTED_MEDIA_TYPE)
          .title("Not supported")
          .build(),
      ExceptionRule.builder()
          .exception(BadRequestException.class)
          .responseStatus(BAD_REQUEST)
          .title("Error while processing the request")
          .build(),
      ExceptionRule.builder()
          .exception(InvalidConfigurationException.class)
          .responseStatus(BAD_REQUEST)
          .title("Bad configuration")
          .build(),
      ExceptionRule.builder()
          .exception(TemplatingException.class)
          .responseStatus(INTERNAL_SERVER_ERROR)
          .title("Templating went wrong")
          .build());

  public static Optional<ExceptionRule> getExceptionRule(Throwable throwable) {
    return MAPPING.stream()
        .filter(rule -> rule.getException()
            .isAssignableFrom(throwable.getClass()))
        .findFirst();
  }
}
