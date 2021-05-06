package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EnumerationConfiguration {

  @NotEmpty
  private List<String> values = new ArrayList<>();

}
