package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class FilterConfiguration {

  private String field;

  @JsonProperty("default")
  private Map<String,Object> defaultValues;
}
