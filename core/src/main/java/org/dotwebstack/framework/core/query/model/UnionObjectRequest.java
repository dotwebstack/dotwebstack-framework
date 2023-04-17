package org.dotwebstack.framework.core.query.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectType;

@Getter
@Builder
public class UnionObjectRequest implements ObjectRequest {

  private final List<SingleObjectRequest> objectRequests;

  @Override
  public ContextCriteria getContextCriteria() {
    return null;
  }
}
