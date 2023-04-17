package org.dotwebstack.framework.core.model;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.Data;

@Data
public class Context {

  @Valid
  private Map<String, ContextField> fields;
}
