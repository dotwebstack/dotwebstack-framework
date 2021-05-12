package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data

public class EqualsFilterCriteria implements FilterCriteria {
  private final FieldConfiguration field;
  private final Object value;
}
