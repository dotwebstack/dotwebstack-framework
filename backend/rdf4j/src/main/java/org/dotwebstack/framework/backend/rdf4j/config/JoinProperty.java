package org.dotwebstack.framework.backend.rdf4j.config;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinProperty {

  @NotBlank
  private String name;

  @NotBlank
  private String referencedField;
}
