package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AndFilterCriteria implements FilterCriteria {
  private List<FilterCriteria> filterCriterias;
}
