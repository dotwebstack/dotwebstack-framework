package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.base.CaseFormat;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinColumn {

  @NotBlank
  private String name;

  // TODO: validate that either referencedField or referencedColumn is set
  private String referencedField;

  private String referencedColumn;

  // TODO get from ObjectField config?
  public String getReferencedColumn() {
    // Lazy-determine referenced column name
    if (referencedColumn == null) {
      referencedColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, referencedField);
    }

    return referencedColumn;
  }
}
