package org.dotwebstack.framework.backend.rdf4j.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinColumn {

  @NotBlank
  private String name;

  @NotBlank
  private String referencedField;
}
