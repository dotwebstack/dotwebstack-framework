package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class DotWebStackConfig {
  @JsonProperty("typeMapping")
  private Map<String, BackendConfig> typeMapping;
}
