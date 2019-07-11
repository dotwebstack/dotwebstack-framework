package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectiveContainerTuple {

  private GraphQLDirectiveContainer container;

  private Object value;

}
