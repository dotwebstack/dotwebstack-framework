package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ContextFieldConfiguration {
  @NonNull
  private String type;

  @JsonProperty("default")
  private String defaultValue;
}
