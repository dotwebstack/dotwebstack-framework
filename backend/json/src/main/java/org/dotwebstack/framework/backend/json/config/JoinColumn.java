package org.dotwebstack.framework.backend.json.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinColumn {

  @NotBlank
  private String name;

  @NotBlank
  private String referencedField;
}
