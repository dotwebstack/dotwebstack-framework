package org.dotwebstack.framework.core.query;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraphQlField {
  private final String name;

  private final String type;

  boolean listType;

  @Builder.Default
  private final List<GraphQlField> fields = Collections.emptyList();

  @Builder.Default
  private final List<GraphQlArgument> arguments = Collections.emptyList();
}
