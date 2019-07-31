package org.dotwebstack.framework.core.query;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GraphQlArgumentObject extends GraphQlArgument {
  @Builder.Default
  private List<GraphQlArgument> arguments = Collections.emptyList();
}
