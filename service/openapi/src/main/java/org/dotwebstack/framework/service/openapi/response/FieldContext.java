package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldContext {

  private Object data;

  private Map<String, Object> input;
}
