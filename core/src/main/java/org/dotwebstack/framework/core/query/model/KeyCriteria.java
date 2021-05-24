package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class KeyCriteria {
  private Map<String, Object> values;
}
