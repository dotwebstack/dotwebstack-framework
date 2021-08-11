package org.dotwebstack.framework.service.openapi.response.dwssettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DwsQuerySettings {
  private String queryName;

  @Builder.Default
  private List<String> requiredFields = new ArrayList<>();

  @Builder.Default
  private List<QueryFilter> filters = new ArrayList<>();

  @Builder.Default
  private Map<String, String> keys = new HashMap<>();

  @Builder.Default
  private List<QueryPaging> pagings = new ArrayList<>();

}
