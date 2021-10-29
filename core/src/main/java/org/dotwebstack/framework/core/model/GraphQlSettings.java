package org.dotwebstack.framework.core.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GraphQlSettings {

  @NotBlank
  private String proxy;
}
