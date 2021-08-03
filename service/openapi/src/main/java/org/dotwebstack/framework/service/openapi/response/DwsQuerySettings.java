package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class DwsQuerySettings {
  private String queryName;

  private List<String> requiredFields;

}
