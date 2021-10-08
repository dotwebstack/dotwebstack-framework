package org.dotwebstack.framework.backend.postgres.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinColumn {

  @NotBlank
  private String name;

  @NotBlank
  private String referencedField;
}
