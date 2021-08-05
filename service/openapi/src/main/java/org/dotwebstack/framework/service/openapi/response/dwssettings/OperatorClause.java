package org.dotwebstack.framework.service.openapi.response.dwssettings;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OperatorClause {
  private String operator;

  private String parameterName;

  private String parameterType;
}
