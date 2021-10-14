package org.dotwebstack.framework.core.backend.filter;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterCriteria {

  private final List<ObjectFieldPath> fieldPath;

  private final Map<String, Object> value;
}
