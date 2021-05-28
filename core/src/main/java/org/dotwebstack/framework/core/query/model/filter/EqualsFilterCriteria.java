package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class EqualsFilterCriteria implements FilterCriteria {
  private FieldConfiguration field;

  private Object value;
}
