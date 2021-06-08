package org.dotwebstack.framework.core.query.model.filter;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class EqualsFilterCriteria implements FilterCriteria {
  private FieldPath fieldPath;

  private Object value;
}
