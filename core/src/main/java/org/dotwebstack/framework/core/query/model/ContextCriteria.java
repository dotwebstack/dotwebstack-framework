package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class ContextCriteria {
  @NonNull
  private final String field;

  @NonNull
  private final Object value;
}
