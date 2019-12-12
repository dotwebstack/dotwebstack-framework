package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;

@Builder
@Getter
public class FilterRule {
  @Builder.Default
  private List<GraphQLFieldDefinition> path = new ArrayList<>();

  private String operator;

  private Object value;

  private GraphQLObjectType objectType;

  public boolean isResource() {
    if (path.size() == 1) {
      return Objects.nonNull(path.get(0)
          .getDirective(Rdf4jDirectives.RESOURCE_NAME));
    }
    return false;
  }
}
