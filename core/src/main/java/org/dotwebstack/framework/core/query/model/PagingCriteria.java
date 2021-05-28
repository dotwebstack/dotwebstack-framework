package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagingCriteria {
  private final int page;

  private final int pageSize;
}
