package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLFieldDefinition;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderBy {
  private List<GraphQLFieldDefinition> fieldPath;

  private String order;
}
