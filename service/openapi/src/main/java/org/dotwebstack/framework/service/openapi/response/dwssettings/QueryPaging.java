package org.dotwebstack.framework.service.openapi.response.dwssettings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryPaging {

  private String firstParam;

  private String offsetParam;
}
