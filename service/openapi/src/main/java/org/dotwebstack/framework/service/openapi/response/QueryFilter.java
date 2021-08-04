package org.dotwebstack.framework.service.openapi.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryFilter {
  private String[] field;

  @Builder.Default
  private List<QueryFilterClause> clauses = new ArrayList<>();
}
