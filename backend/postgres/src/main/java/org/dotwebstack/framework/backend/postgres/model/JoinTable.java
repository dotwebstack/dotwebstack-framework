package org.dotwebstack.framework.backend.postgres.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
