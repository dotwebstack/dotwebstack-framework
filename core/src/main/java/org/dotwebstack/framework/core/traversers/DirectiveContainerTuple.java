package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import lombok.Data;
import lombok.NonNull;

@Data
public class DirectiveContainerTuple {
  private GraphQLDirectiveContainer container;

  private Object value;

  public DirectiveContainerTuple(@NonNull GraphQLDirectiveContainer container, Object value) {
    this.container = container;
    this.value = value;
  }
}
