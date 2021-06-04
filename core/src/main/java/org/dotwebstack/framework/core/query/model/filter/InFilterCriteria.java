package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class InFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  private final List<?> values;
}
