package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class InFilterCriteria extends AbstractFilterCriteria {
  private final FieldConfiguration field;

  private final List<?> values;
}
