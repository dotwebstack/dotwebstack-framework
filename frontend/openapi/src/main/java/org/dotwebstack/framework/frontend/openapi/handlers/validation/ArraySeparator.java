package org.dotwebstack.framework.frontend.openapi.handlers.validation;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.Collection;
import javax.annotation.Nullable;

/**
 * @see <a href=
 * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#parameterObject">
 *  OAIspec</a>
 */
// All classes in this package are copied from
// atlassian's swagger-request-validator
class ArraySeparator {

  static ArraySeparator from(final Parameter parameter) {
    if (parameter.getStyle() == null) {
      // See https://github.com/swagger-api/swagger-parser/issues/690 - mapping from Swagger 2.0
      // isn't fully implemented yet
      return new ArraySeparator(",", false);
    }
    final boolean explode = TRUE.equals(parameter.getExplode());
    switch (parameter.getStyle()) {
      case SIMPLE:
        return new ArraySeparator(",", false);
      case MATRIX:
        return explode ? new ArraySeparator(null, true) : new ArraySeparator(",", false);
      case LABEL:
        return new ArraySeparator("\\.", false);
      case FORM:
        return explode ? new ArraySeparator(null, true) : new ArraySeparator(",", false);
      case SPACEDELIMITED:
        return explode ? new ArraySeparator(null, false) : new ArraySeparator(" ", false);
      case PIPEDELIMITED:
        return explode ? new ArraySeparator(null, false) : new ArraySeparator("\\|", false);
      default:
        // See https://github.com/swagger-api/swagger-parser/issues/690 - mapping from Swagger 2.0
        // isn't fully implemented yet
        return new ArraySeparator(",", false);
    }
  }

  private final String separator;
  private final boolean isMultiValueParam;

  ArraySeparator(@Nullable final String separator, final boolean isMultiValueParam) {
    this.separator = separator;
    this.isMultiValueParam = isMultiValueParam;
  }

  boolean isMultiValueParam() {
    return isMultiValueParam;
  }

  Collection<String> split(final String value) {
    if (separator == null) {
      return singletonList(value);
    }
    return asList(value.split(separator));
  }
}


