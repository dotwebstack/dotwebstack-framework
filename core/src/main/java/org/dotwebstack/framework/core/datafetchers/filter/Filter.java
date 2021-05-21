package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;

@Data
@Builder
class Filter {
  private GraphQLInputObjectField inputObjectField;

  private FilterConfiguration filterConfiguration;

  private FieldConfiguration fieldConfiguration;

  private Map<String, Object> data;
}
