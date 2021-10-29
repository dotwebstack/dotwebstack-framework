package org.dotwebstack.framework.core.model;

import javax.validation.Valid;
import lombok.Data;

@Data
public class Settings {

  @Valid
  private GraphQlSettings graphql;
}
