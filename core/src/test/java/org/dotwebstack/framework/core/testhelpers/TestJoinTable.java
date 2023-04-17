package org.dotwebstack.framework.core.testhelpers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
class TestJoinTable {

  @NotBlank
  private String name;

  @Valid
  private List<TestJoinColumn> joinColumns;

  @Valid
  private List<TestJoinColumn> inverseJoinColumns;
}
