package org.dotwebstack.framework.service.openapi.query.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Key {

  private String fieldPath;

  private Object value;

}
