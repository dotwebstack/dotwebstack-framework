package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldContext {

  private final Object data;

  private final Map<String, Object> input;
}
