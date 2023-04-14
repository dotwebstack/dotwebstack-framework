package org.dotwebstack.framework.core.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldArgument {

  @NotBlank
  private String name;

  @NotBlank
  private String type;

  private boolean nullable = false;

  private boolean list = false;
}
