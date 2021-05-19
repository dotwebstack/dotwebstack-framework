package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class GreaterThenEqualsFilterCriteria implements FilterCriteria {
  private final FieldConfiguration field;

  private final Object value;
}
