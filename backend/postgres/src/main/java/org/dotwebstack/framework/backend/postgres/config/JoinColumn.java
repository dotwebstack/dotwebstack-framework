package org.dotwebstack.framework.backend.postgres.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinColumn {

  @NotBlank
  private String name;

  private String referencedField;

  private String referencedColumn;

  public String getField() {
    return referencedField != null ? referencedField : referencedColumn;
  }
}
