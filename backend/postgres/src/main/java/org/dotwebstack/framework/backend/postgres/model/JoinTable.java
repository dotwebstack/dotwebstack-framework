package org.dotwebstack.framework.backend.postgres.model;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinTable {

  @NotBlank
  private String name;

  @Valid
  private List<JoinColumn> joinColumns;

  @Valid
  private List<JoinColumn> inverseJoinColumns;
}
