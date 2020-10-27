package org.dotwebstack.framework.service.openapi.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
class ExceptionRule {

  private final Class<?> exception;

  private final HttpStatus responseStatus;

  private final String title;

  private final boolean detail;
}
