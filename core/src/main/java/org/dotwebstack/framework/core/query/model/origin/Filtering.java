package org.dotwebstack.framework.core.query.model.origin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

@Data
public class Filtering implements Origin {

  @EqualsAndHashCode.Exclude
  private final FilterCriteria filterCriteria;
}
