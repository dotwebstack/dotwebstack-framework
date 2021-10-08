package org.dotwebstack.framework.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ContextField {
  @NonNull
  private String type;

  @JsonProperty("default")
  private String defaultValue;
}
