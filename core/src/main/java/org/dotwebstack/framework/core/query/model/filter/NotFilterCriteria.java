package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
public class NotFilterCriteria implements FilterCriteria {
  @Getter
  private final FilterCriteria filterCriteria;

  @Override
  public FieldPath getFieldPath() {
    return filterCriteria.getFieldPath();
  }
}
