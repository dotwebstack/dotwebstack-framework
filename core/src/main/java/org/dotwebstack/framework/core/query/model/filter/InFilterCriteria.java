package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
public class InFilterCriteria extends AbstractFilterCriteria {
  private final FieldConfiguration field;

  private final List<?> values;
}
