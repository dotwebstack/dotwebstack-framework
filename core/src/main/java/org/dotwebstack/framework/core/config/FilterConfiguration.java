package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FilterConfiguration {

  private FilterType type = FilterType.EXACT;

  private String field;

  @JsonProperty("default")
  private Object defaultValue;

  public boolean hasDefaultValue() {
    return defaultValue != null;
  }
}
