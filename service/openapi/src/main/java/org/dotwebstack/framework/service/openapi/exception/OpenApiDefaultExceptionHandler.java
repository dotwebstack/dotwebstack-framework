package org.dotwebstack.framework.service.openapi.exception;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.service.openapi.exception.ExceptionRuleHelper.getExceptionRule;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OpenApiDefaultExceptionHandler {

  public Mono<Void> handle(Throwable throwable) {
    if (throwable instanceof ResponseStatusException) {
      return Mono.error(throwable);
    }
    ExceptionRule exceptionRule = getExceptionRule(throwable).orElseThrow();

    return Mono.error(getResponseStatusException(throwable, exceptionRule));
  }

  private Throwable getResponseStatusException(Throwable throwable, ExceptionRule exceptionRule) {
    if (!exceptionRule.isDetails()) {
      return getResponseStatusExceptionWithoutDetails(throwable, exceptionRule.getHttpStatus());
    }

    if (isNotEmpty(exceptionRule.getReason())) {
      return getResponseStatusException(throwable, exceptionRule.getHttpStatus(), exceptionRule.getReason());
    }

    return getResponseStatusException(exceptionRule.getHttpStatus());
  }

  private Throwable getResponseStatusException(HttpStatus status) {
    return new ResponseStatusException(status);
  }

  private Throwable getResponseStatusException(Throwable throwable, HttpStatus status, String reason) {
    String message = format("[OpenApi] An Exception occurred [%s] resulting in [%d] reason [%s]",
        throwable.getMessage(), status.value(), reason);
    return new ResponseStatusException(status, message);
  }

  private Throwable getResponseStatusExceptionWithoutDetails(Throwable throwable, HttpStatus status) {
    String requestId = UUID.randomUUID()
        .toString();
    LOG.info(format("[OpenApi] An Exception occurred [%s] resulting in [%d]", throwable.getMessage(), status.value()));
    String message = format("An error occured from which the server was unable to recover. "
        + "Please contact the API maintainer with the following details: '%s'", requestId);
    return new ResponseStatusException(status, message);
  }
}
