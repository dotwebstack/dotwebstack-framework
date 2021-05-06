package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContextArgumentConfiguration {

  @NotNull
  private ContextArgumentType type;

  private boolean required;

  @NotNull
  @JsonProperty("default")
  private Object defaultValue;

  @NotBlank
  private String filterExpr;

}
