package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class NotFilterCriteria implements FilterCriteria {
  private FilterCriteria filterCriteria;

  @Override
  public FieldConfiguration getField() {
    return filterCriteria.getField();
  }

  @Override
  public String[] getFieldPath() {
    return filterCriteria.getFieldPath();
  }
}
