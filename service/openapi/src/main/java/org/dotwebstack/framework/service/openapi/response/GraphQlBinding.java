package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class GraphQlBinding {
  private String queryName;

  private String selector;

  private boolean collection;
}
