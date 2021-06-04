package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotFilterCriteria implements FilterCriteria {
  private FilterCriteria filterCriteria;

  @Override
  public FieldPath getFieldPath() {
    return null;
  }
}
