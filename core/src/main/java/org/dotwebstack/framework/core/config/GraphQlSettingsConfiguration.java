package org.dotwebstack.framework.core.config;

import javax.validation.Valid;
import lombok.Data;

@Data
public class GraphQlSettingsConfiguration {

  @Valid
  private String proxy;
}
