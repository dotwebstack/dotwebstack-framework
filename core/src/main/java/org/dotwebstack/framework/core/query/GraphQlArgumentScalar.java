package org.dotwebstack.framework.core.query;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GraphQlArgumentScalar {
  private final String value;
}
