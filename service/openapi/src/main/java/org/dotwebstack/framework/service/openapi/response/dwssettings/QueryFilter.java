package org.dotwebstack.framework.service.openapi.response.dwssettings;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryFilter {
  private String[] fieldPath;

  private String type;

  private Map<?, ?> fieldFilters;

}
