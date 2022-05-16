package org.dotwebstack.framework.core.datafetchers.aggregate;

import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.NUMERIC_FUNCTIONS;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;

import graphql.schema.SelectedField;
import java.util.Map;
import org.dotwebstack.framework.core.config.EnumerationConfiguration;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public class AggregateValidator {

  private AggregateValidator() {}

  public static void validate(Map<String, EnumerationConfiguration> enumerations, ObjectType<?> objectType,
      SelectedField selectedField) {

    var fieldName = (String) selectedField.getArguments()
        .get(AggregateConstants.FIELD_ARGUMENT);

    var objectField = (ObjectField) objectType.getFields()
        .get(fieldName);

    if (NUMERIC_FUNCTIONS.contains(selectedField.getName())) {
      validateNumericField(objectField);
    } else {
      validateOtherFields(enumerations, selectedField, objectField);
    }
  }

  private static void validateNumericField(ObjectField objectField) {
    if (isNotNumeric(objectField)) {
      throw requestValidationException(
          String.format("Numeric aggregation for non-numeric field %s is not supported.", objectField.getName()));
    }
  }

  private static boolean isNotNumeric(ObjectField objectField) {
    return !(GraphQLInt.getName()
        .equals(objectField.getType())
        || GraphQLFloat.getName()
            .equals(objectField.getType()));
  }

  private static void validateOtherFields(Map<String, EnumerationConfiguration> enumerations,
      SelectedField selectedField, ObjectField objectField) {
    switch (selectedField.getName()) {
      case STRING_JOIN_FIELD:
        validateStringJoinField(enumerations, objectField);
        break;
      case COUNT_FIELD:
        // no additional validation needed
        break;
      default:
        throw requestValidationException(
            String.format("Unsupported aggregation function: %s.", selectedField.getName()));
    }
  }

  private static void validateStringJoinField(Map<String, EnumerationConfiguration> enumerations,
      ObjectField objectField) {
    if (isNotText(enumerations, objectField)) {
      throw requestValidationException(
          String.format("String aggregation for non-text field %s is not supported.", objectField.getName()));
    }
  }

  private static boolean isNotText(Map<String, EnumerationConfiguration> enumerations, ObjectField objectField) {
    return !(GraphQLString.getName()
        .equals(objectField.getType()) || enumerations.containsKey(objectField.getType()));
  }
}
