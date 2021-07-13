package org.dotwebstack.framework.core.config;

import javax.validation.Valid;
import lombok.Data;

@Data
public class SettingsConfiguration {

  @Valid
  private GraphQlSettingsConfiguration graphql;
}
