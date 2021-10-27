package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.core.model.Context;

@Builder
@Getter
@EqualsAndHashCode
public class ContextCriteria {
  @NonNull
  private final String name;

  @NonNull
  private final Context context;

  @NonNull
  private final Map<String, Object> values;
}
