package org.dotwebstack.framework.core.model;

import javax.validation.Valid;
import lombok.Data;

@Data
public class GraphQlSettings {

  @Valid
  private String proxy;
}
