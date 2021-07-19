package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class ContextCriteria {
  @NonNull
  private final String field;

  @NonNull
  private final Object value;
}
