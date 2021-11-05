package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class BatchRequest {

  @NonNull
  private final ObjectRequest objectRequest;

  @NonNull
  private final Set<Map<String,Object>> keys;
}
