package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class SortCriteria {

  // TODO: Field moet FieldConfiguration worden + aparte String fieldPath
  // TODO: ValidSortAndFilterFields uitbreiden zodat deze een Map<String, FieldConfiguration> terug
  // geeft?
  private final String field;

  private final SortDirection direction;

  public boolean hasNestedField() {
    return getFieldPath().length > 0;
  }

  public String[] getFieldPath() {
    return StringUtils.split(field, '.');
  }
}
