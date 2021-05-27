package org.dotwebstack.framework.core.query;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GraphQlArgumentObject {
  @Builder.Default
  private final List<GraphQlArgument> arguments = Collections.emptyList();
}
