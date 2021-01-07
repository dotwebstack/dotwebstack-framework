package org.dotwebstack.framework.core.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KeyConfiguration {

  @NotBlank
  private String field;
}
