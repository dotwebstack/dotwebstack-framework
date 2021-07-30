package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class DwsQuerySettings {
  private final String queryName;

  private final List<String> requiredFields;

}
