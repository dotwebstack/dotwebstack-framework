package org.dotwebstack.framework.core.config;

import java.util.Map;
import lombok.Data;

@Data
public class ContextConfiguration {

  private Map<String, ContextFieldConfiguration> fields;
}
