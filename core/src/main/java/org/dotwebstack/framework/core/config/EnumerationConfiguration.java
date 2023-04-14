package org.dotwebstack.framework.core.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class EnumerationConfiguration {

  @NotEmpty
  private List<String> values = new ArrayList<>();

}
