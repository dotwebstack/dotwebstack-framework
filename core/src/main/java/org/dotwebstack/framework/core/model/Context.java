package org.dotwebstack.framework.core.model;

import java.util.Map;
import javax.validation.Valid;
import lombok.Data;

@Data
public class Context {

  @Valid
  private Map<String, ContextField> fields;
}
