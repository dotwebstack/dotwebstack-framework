package org.dotwebstack.framework.core.config;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestJoinTable {
  @NotBlank
  private String name;

  @Valid
  private List<TestJoinColumn> joinColumns;

  @Valid
  private List<TestJoinColumn> inverseJoinColumns;
}
