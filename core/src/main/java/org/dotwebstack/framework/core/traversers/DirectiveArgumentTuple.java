package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import lombok.Data;
import lombok.NonNull;

@Data
public class DirectiveArgumentTuple {
  private GraphQLDirectiveContainer argument;

  private Object value;

  public DirectiveArgumentTuple(@NonNull GraphQLDirectiveContainer directiveContainer, @NonNull Object value) {
    this.argument = directiveContainer;
    this.value = value;
  }
}
