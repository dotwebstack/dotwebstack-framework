package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class SortCriteria {

  private final String field;

  private final SortDirection direction;

  public static boolean hasNestedField(SortCriteria sortCriteria) {
    return sortCriteria.getFieldPath().length > 0;
  }

  public String[] getFieldPath() {
    return StringUtils.split(field, '.');
  }
}
