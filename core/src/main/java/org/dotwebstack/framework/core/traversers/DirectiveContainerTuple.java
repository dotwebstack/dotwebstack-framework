package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DirectiveContainerTuple {

  private GraphQLDirectiveContainer container;

  private Object value;

}
