package org.dotwebstack.framework.core.testhelpers;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
@TestValidJoinColumn
public class TestJoinColumn {

  @NotBlank
  private String name;

  private String referencedField;

  private String referencedColumn;
}
