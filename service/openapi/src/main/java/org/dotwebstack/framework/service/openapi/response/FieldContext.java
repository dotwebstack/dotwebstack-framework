package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FieldContext {

  private Object data;

  private Map<String, Object> input;
}
