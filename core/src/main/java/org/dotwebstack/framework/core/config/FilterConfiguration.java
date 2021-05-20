package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
public class FilterConfiguration {

  private String field;

  @JsonProperty("default")
  private Map<String, Object> defaultValues;
}
