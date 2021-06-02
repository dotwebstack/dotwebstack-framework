package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
public class EqualsFilterCriteria extends AbstractFilterCriteria {
  private FieldConfiguration field;

  private Object value;
}
