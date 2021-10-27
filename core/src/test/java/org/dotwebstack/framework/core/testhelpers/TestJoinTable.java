package org.dotwebstack.framework.core.testhelpers;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
