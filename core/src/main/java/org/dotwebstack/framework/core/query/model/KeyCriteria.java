package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyCriteria {
  private Map<String, Object> values;
}
