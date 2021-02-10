package org.dotwebstack.framework.backend.postgres.config;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinTable {

  @NotBlank
  private String name;

  @Valid
  private List<JoinColumn> joinColumns;

  @Valid
  private List<JoinColumn> inverseJoinColumns;
}
