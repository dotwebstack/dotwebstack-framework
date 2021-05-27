package org.dotwebstack.framework.core.query;

import graphql.language.Type;
import graphql.language.Value;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraphQlArgument {
  private final String name;

  private final String baseType;

  private final Type<?> type;

  private final boolean required;

  private final Value<?> defaultValue;

  @Builder.Default
  private final List<GraphQlArgument> children = Collections.emptyList();
}
