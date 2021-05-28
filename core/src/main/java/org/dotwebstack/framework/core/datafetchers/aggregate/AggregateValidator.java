package org.dotwebstack.framework.core.datafetchers.aggregate;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.NUMERIC_FUNCTIONS;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;

import graphql.schema.SelectedField;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;

public class AggregateValidator {

  private AggregateValidator() {}

  public static void validate(AbstractFieldConfiguration aggregateFieldConfiguration, SelectedField selectedField) {

    if (NUMERIC_FUNCTIONS.contains(selectedField.getName())) {
      if (aggregateFieldConfiguration.isNumeric()) {
        return;
      } else {
        throw new IllegalArgumentException(String.format(
            "Numeric aggregation for non-numeric field %s is not supported.", aggregateFieldConfiguration.getName()));
      }
    }
    switch (selectedField.getName()) {
      case STRING_JOIN_FIELD:
        validateStringJoinField(aggregateFieldConfiguration);
        break;
      case COUNT_FIELD:
        // no additional validation needed
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported aggregation function: %s.", selectedField.getName()));
    }
  }

  private static void validateStringJoinField(AbstractFieldConfiguration aggregateFieldConfiguration) {
    if (!aggregateFieldConfiguration.isText()) {
      throw new IllegalArgumentException(String.format("String aggregation for non-text field %s is not supported.",
          aggregateFieldConfiguration.getName()));
    }
  }
}
