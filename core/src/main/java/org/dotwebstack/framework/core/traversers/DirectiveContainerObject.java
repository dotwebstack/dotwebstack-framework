package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectiveContainerObject {

  private GraphQLDirectiveContainer container;

  private GraphQLObjectType objectType;

  private GraphQLFieldDefinition fieldDefinition;

  private Map<String, Object> requestArguments;

  private Object value;

  private boolean isResource;

}
