package org.dotwebstack.framework.core.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldArgumentConfiguration {

  @NotBlank
  private String name;

  @NotBlank
  private String type;

  private boolean nullable = false;

  private boolean list = false;

}
