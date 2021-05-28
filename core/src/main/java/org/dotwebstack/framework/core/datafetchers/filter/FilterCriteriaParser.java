package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

public interface FilterCriteriaParser {
  boolean supports(GraphQLInputObjectField inputObjectField);

  List<FilterCriteria> parse(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data);
}
