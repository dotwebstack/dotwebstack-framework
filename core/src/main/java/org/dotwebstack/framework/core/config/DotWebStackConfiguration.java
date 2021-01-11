package org.dotwebstack.framework.core.config;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DotWebStackConfiguration {

  @NotNull
  @Valid
  private Map<String, TypeConfiguration<?>> typeMapping;
}
