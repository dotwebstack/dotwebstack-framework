package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortCriteria {

  private final String field;

  private final SortDirection direction;
}
