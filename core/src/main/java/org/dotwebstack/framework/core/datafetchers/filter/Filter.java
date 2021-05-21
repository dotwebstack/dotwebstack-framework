package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class Filter {
  private GraphQLInputObjectField inputObjectField;

  private Object data;
}
