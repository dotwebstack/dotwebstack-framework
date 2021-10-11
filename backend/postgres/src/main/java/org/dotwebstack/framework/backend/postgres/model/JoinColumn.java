package org.dotwebstack.framework.backend.postgres.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinColumn {

  @NotBlank
  private String name;

  // TODO: validate that either referencedField or referencedColumn is set
  private String referencedField;
}
