package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FilterRule {
  private List<GraphQLFieldDefinition> path;

  private String operator;

  private Object value;

  private boolean isResource;

  private GraphQLObjectType objectType;
}
