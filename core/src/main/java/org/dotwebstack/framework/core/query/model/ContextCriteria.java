package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class ContextCriteria {
  @NonNull
  private String field;

  @NonNull
  private Object value;
}
