package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

import java.util.List;

@Data
@Builder
public class InFilterCriteria implements FilterCriteria {
  private final FieldConfiguration field;

  private final List<Object> values;
}
