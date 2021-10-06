package org.dotwebstack.framework.core.backend.filter;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.model.ObjectField;

@Data
@Builder
public class BackendFilterCriteria {

  private final List<ObjectField> fieldPath;

  private final Map<String, Object> value;
}
