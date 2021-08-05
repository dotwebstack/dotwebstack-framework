package org.dotwebstack.framework.service.openapi.response.dwssettings;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldClause {
  private String fieldName;

  private boolean negate;

  @Builder.Default
  private List<OperatorClause> clauses = new ArrayList<>();

}
