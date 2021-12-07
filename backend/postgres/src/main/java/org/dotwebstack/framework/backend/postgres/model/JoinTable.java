package org.dotwebstack.framework.backend.postgres.model;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinTable {

  @NotBlank
  private String name;

  @Valid
  @NotNull
  private List<JoinColumn> joinColumns;

  @Valid
  @NotNull
  private List<JoinColumn> inverseJoinColumns;
}
