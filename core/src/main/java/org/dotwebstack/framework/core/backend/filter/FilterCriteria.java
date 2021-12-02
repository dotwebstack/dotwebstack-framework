package org.dotwebstack.framework.core.backend.filter;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectField;

@Data
@Builder
public class FilterCriteria {

  @NonNull
  @Builder.Default
  private final FilterType filterType = FilterType.EXACT;

  @NonNull
  private final List<ObjectField> fieldPath;

  @NonNull
  private final Map<String, Object> value;
}
