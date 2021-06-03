package org.dotwebstack.framework.core.query.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EqualsFilterCriteria extends AbstractFilterCriteria {
  private FieldConfiguration field;

  private Object value;
}
