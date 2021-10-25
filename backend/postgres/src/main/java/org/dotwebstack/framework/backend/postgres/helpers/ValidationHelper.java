package org.dotwebstack.framework.backend.postgres.helpers;

import static java.util.stream.Collectors.joining;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;

public final class ValidationHelper {

  private ValidationHelper() {}

  public static void validateFields(Object object) {
    Set<ConstraintViolation<Object>> violations = Validation.buildDefaultValidatorFactory()
        .getValidator()
        .validate(object);

    if (!violations.isEmpty()) {
      String msg = String.format("%s has validation errors (%s):%n", object.getClass(), violations.size());
      String violationLines = violations.stream()
          .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
          .collect(joining(", " + System.lineSeparator()));
      throw new ConstraintViolationException(msg + violationLines, violations);
    }
  }
}
