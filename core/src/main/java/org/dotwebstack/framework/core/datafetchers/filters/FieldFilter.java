package org.dotwebstack.framework.core.datafetchers.filters;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldFilter implements Filter {

  private final String field;

  private final Object value;
}
