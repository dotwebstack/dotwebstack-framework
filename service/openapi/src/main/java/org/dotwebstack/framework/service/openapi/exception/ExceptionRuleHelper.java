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
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapperException;
import org.springframework.http.HttpStatus;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

public class ExceptionRuleHelper {
  public static final List<ExceptionRule> SIMPLE_RULES = List.of(ExceptionRule.builder()
      .exception(NotAcceptableException.class)
      .httpStatus(NOT_ACCEPTABLE)
      .reason("Unsupported media type requested.")
      .build(),
      ExceptionRule.builder()
          .exception(ParameterValidationException.class)
          .httpStatus(BAD_REQUEST)
          .reason("Error while obtaining request parameters.")
          .build(),
      ExceptionRule.builder()
          .exception(ResponseMapperException.class)
          .httpStatus(INTERNAL_SERVER_ERROR)
          .details(false)
          .build(),
      ExceptionRule.builder()
          .exception(GraphQlErrorException.class)
          .httpStatus(INTERNAL_SERVER_ERROR)
          .details(false)
          .build(),
      ExceptionRule.builder()
          .exception(NoContentException.class)
          .httpStatus(NO_CONTENT)
          .build(),
      ExceptionRule.builder()
          .exception(NotFoundException.class)
          .httpStatus(NOT_FOUND)
          .reason("No results found.")
          .build(),
      ExceptionRule.builder()
          .exception(UnsupportedOperationException.class)
          .httpStatus(UNSUPPORTED_MEDIA_TYPE)
          .reason("Not supported.")
          .build(),
      ExceptionRule.builder()
          .exception(BadRequestException.class)
          .httpStatus(BAD_REQUEST)
          .reason("Error while processing the request.")
          .build(),
      ExceptionRule.builder()
          .exception(InvalidConfigurationException.class)
          .httpStatus(BAD_REQUEST)
          .reason("Bad configuration")
          .build(),
      ExceptionRule.builder()
          .exception(TemplatingException.class)
          .httpStatus(INTERNAL_SERVER_ERROR)
          .reason("Templating went wrong!")
          .build());

  public static Optional<HttpStatus> getResponseStatus(Throwable throwable) {
    return getExceptionRule(throwable).map(ExceptionRule::getHttpStatus)
        .or(() -> Optional.of(throwable)
            .filter(t -> t instanceof ThrowableProblem)
            .map(ThrowableProblem.class::cast)
            .map(ExceptionRuleHelper::getResponseStatus));
  }

  public static HttpStatus getResponseStatus(ThrowableProblem throwableProblem) {
    return Optional.of(throwableProblem)
        .map(ThrowableProblem::getStatus)
        .map(StatusType::getStatusCode)
        .map(HttpStatus::valueOf)
        .orElseThrow();
  }

  public static Optional<ExceptionRule> getExceptionRule(Throwable throwable) {
    if (throwable instanceof ThrowableProblem) {
      ThrowableProblem throwableProblem = (ThrowableProblem) throwable;
      return Optional.of(ExceptionRule.builder()
          .httpStatus(getResponseStatus(throwableProblem))
          .reason(throwable.getMessage())
          .build());
    }

    return SIMPLE_RULES.stream()
        .filter(rule -> rule.getException()
            .isAssignableFrom(throwable.getClass()))
        .findFirst();
  }
}
