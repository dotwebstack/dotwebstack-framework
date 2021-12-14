package org.dotwebstack.framework.core.backend.filter;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupFilterCriteria implements FilterCriteria {
  private GroupFilterOperator logicalOperator;

  private List<FilterCriteria> filterCriterias;
}
