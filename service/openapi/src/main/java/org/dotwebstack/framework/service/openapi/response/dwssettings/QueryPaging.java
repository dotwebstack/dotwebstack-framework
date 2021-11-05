package org.dotwebstack.framework.service.openapi.response.dwssettings;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class QueryPaging {

  @NonNull
  private String pageParam;

  @NonNull
  private String pageSizeParam;

}
