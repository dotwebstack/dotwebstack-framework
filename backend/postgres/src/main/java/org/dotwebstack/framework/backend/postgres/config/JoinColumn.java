package org.dotwebstack.framework.backend.postgres.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
// @AllArgsConstructor
// TODO: check setters and getters
public class JoinColumn {

  @NotBlank
  private String name;

  // TODO: wat gaan we hiermee doen?
  // @NotBlank
  @Setter
  private String referencedField;

  @Setter
  private String referencedColumn;

  public JoinColumn(String name, String referencedField) {
    this.name = name;
    this.referencedField = referencedField;
  }

  public String getField() {
    return referencedField != null ? referencedField : referencedColumn;
  }
}
