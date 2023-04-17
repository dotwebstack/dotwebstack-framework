package org.dotwebstack.framework.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContextField {
  @NotBlank
  private String type;

  @JsonProperty("default")
  private String defaultValue;
}
