package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@EqualsAndHashCode
public class BatchRequest {

  @NonNull
  private final ObjectRequest objectRequest;

  @NonNull
  private final Set<Map<String, Object>> keys;
}
