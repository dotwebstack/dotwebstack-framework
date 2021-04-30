package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectTypesConfiguration {

  private Map<String, AbstractTypeConfiguration<?>> objectTypes;
}
