package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import lombok.Data;

@Data
public class DirectiveArgumentTuple {
  private GraphQLDirectiveContainer argument;
  private Object value;

  public DirectiveArgumentTuple(GraphQLDirectiveContainer directiveContainer, Object value) {
    this.argument = directiveContainer;
    this.value = value;
  }
}
