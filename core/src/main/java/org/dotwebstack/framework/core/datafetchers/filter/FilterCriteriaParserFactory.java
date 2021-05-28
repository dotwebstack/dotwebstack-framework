package org.dotwebstack.framework.core.datafetchers.filter;

import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FilterCriteriaParserFactory {
  private final List<FilterCriteriaParser> filterCriteriaParsers;

  public FilterCriteriaParserFactory(List<FilterCriteriaParser> filterCriteriaParsers) {
    this.filterCriteriaParsers = filterCriteriaParsers;
  }

  public FilterCriteriaParser getFilterCriteriaParser(GraphQLInputObjectField inputObjectField) {
    return filterCriteriaParsers.stream()
        .filter(filterCriteriaParser -> filterCriteriaParser.supports(inputObjectField))
        .findFirst()
        .orElseThrow();
  }
}
