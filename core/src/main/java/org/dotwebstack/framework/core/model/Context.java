package org.dotwebstack.framework.core.model;

import java.util.Map;
import lombok.Data;

@Data
public class Context {
  private Map<String, ContextField> fields;
}
