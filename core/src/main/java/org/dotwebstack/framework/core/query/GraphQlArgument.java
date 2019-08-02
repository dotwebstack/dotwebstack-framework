package org.dotwebstack.framework.core.query;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraphQlArgument {
  private String name;

  private String type;

  @Builder.Default
  private List<GraphQlArgument> children = Collections.emptyList();
}
