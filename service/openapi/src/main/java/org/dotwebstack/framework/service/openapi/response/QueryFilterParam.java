package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryFilterParam {
  private String paramType;

  private String paramName;
}
