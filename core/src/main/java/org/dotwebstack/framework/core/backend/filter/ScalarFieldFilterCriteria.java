package org.dotwebstack.framework.core.backend.filter;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectField;

@Builder
@Getter
public class ScalarFieldFilterCriteria implements FilterCriteria {
  @NonNull
  @Builder.Default
  private final FilterType filterType = FilterType.EXACT;

  @NonNull
  private final List<ObjectField> fieldPath;

  @NonNull
  private final Map<String, Object> value;

  @Builder.Default
  private boolean isCaseSensitive = true;
}
