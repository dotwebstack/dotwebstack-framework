package org.dotwebstack.framework.service.openapi.query;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Select {

  private String fieldPath;

  private String name;

  private Object value;

}
