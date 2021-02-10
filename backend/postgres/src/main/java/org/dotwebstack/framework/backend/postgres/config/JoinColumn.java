package org.dotwebstack.framework.backend.postgres.config;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinColumn {

  @NotBlank
  private String name;

  @NotBlank
  private String referencedField;
}
