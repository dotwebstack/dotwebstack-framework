package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class BooleanFilterCriteriaParser extends AbstractFilterCriteriaParser {

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return Scalars.GraphQLBoolean.equals(inputObjectField.getType());
  }

  @Override
  public List<FilterCriteria> parse(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    // TODO: add fieldName
    return getFilter(typeConfiguration, inputObjectField, data).stream()
        .map(filter -> EqualsFilterCriteria.builder()
            .field(getFieldConfiguration(typeConfiguration, inputObjectField))
            .value(filter.getData())
            .build())
        .collect(Collectors.toList());
  }
}
