package org.dotwebstack.framework.core.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestJoinColumn {
  @NotBlank
  private String name;

  private String referencedField;

  private String referencedColumn;

  public TestJoinColumn(String name, String referencedField) {
    this.name = name;
    this.referencedField = referencedField;
  }

  public String getField() {
    return referencedField != null ? referencedField : referencedColumn;
  }
}
