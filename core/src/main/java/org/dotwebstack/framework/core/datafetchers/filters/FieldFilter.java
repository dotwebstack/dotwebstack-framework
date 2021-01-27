package org.dotwebstack.framework.core.datafetchers.filters;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class FieldFilter implements Filter {

  private final String field;

  private final Object value;
}
