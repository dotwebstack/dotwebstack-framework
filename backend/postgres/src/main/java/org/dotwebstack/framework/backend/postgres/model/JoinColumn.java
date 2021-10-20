package org.dotwebstack.framework.backend.postgres.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.dotwebstack.framework.backend.postgres.model.validation.ValidJoinColumn;

@Data
@ValidJoinColumn
public class JoinColumn {

  @NotBlank
  private String name;

  private String referencedField;

  private String referencedColumn;
}
