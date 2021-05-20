package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;

import java.util.Map;

@Data
@Builder
class Filter {
  private GraphQLInputObjectField inputObjectField;

  private FilterConfiguration filterConfiguration;

  private FieldConfiguration fieldConfiguration;

  private Map<String, Object> data;
}
