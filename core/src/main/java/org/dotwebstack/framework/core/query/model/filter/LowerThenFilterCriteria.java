package org.dotwebstack.framework.core.query.model.filter;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class LowerThenFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  private final Object value;
}
