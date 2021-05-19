package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AndFilterCriteria implements FilterCriteria {
  private List<FilterCriteria> filterCriterias;
}
