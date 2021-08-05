package org.dotwebstack.framework.service.openapi.response.dwssettings;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryFilter {
  private String[] fieldPath;

  private String type;

  @Builder.Default
  private List<FieldClause> clauses = new ArrayList<>();
}
