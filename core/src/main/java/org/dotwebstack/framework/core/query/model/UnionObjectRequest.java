package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnionObjectRequest implements ObjectRequest {

  private final List<SingleObjectRequest> objectRequests;

  @Override
  public ContextCriteria getContextCriteria() {
    return null;
  }
}
