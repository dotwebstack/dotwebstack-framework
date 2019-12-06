package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectiveContainerTuple {

  private GraphQLDirectiveContainer container;

  private GraphQLObjectType objectType;

  private Object value;

}
