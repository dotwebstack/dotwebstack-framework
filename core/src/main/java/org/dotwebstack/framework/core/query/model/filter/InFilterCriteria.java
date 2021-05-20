package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class InFilterCriteria implements FilterCriteria {
  private final FieldConfiguration field;

  private final List<Object> values;
}
