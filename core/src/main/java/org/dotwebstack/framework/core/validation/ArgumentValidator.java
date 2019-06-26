package org.dotwebstack.framework.core.validation;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.language.ArrayValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreInputTypes;

public class ArgumentValidator {

  private final SortFieldValidator inputValueValidator;

  public ArgumentValidator(SortFieldValidator inputValueValidator) {
    this.inputValueValidator = inputValueValidator;
  }

  public void validateArguments(DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
    fieldDefinition.getArguments()
        .stream()
        .filter(fd -> isSortField(fd))
        .findFirst()
        .ifPresent(sortArgument -> validateSortArgument(fieldDefinition, arguments, sortArgument));
  }

  private void validateSortArgument(GraphQLFieldDefinition fieldDefinition, Map<String, Object> arguments,
      GraphQLArgument sortArgument) {
    Object providedSortArgument = arguments.get(sortArgument.getName());
    String sortFieldValue;
    if (providedSortArgument != null) {
      // found in query arguments
      sortFieldValue = getSortFieldValue(providedSortArgument);
    } else {
      // not found in query: get default value from field definition
      Optional<GraphQLArgument> sortFieldArgument = fieldDefinition.getArguments()
          .stream()
          .filter(a -> containsSortFieldField(a))
          .findFirst();
      if (sortFieldArgument.isPresent() && sortFieldArgument.get()
          .getValue() instanceof ArrayValue) {
        Optional<ObjectField> field = ((ArrayValue) (sortFieldArgument.get()
            .getDefaultValue())).getValues()
                .stream()
                .filter(value -> value instanceof ObjectValue)
                .map(value -> ((ObjectValue) value).getObjectFields())
                .flatMap(Collection::stream)
                .filter(objectField -> objectField.getName()
                    .equals(CoreInputTypes.SORT_FIELD_FIELD))
                .findFirst();
        ObjectField objectField =
            field.orElseThrow(() -> ExceptionHelper.illegalArgumentException("Field '{}' not found for sort field '{}'",
                CoreInputTypes.SORT_FIELD_FIELD, field.get()
                    .getName()));
        sortFieldValue = objectField.getValue()
            .toString();
      } else {
        throw ExceptionHelper.invalidConfigurationException("sort field argument not found in fieldDefinition '{}'",
            fieldDefinition);
      }
    }
    this.inputValueValidator.validateSortFieldValue(getTypeName(fieldDefinition.getType()), sortFieldValue);
  }

  private String getSortFieldValue(Object sortArgument) {
    if (!(sortArgument instanceof List)) {
      throw ExceptionHelper.illegalArgumentException("Sort argument '{}' should be a list.", sortArgument);
    }
    @SuppressWarnings("unchecked")
    Optional<Map<String, String>> map = ((List<?>) sortArgument).stream()
        .filter(argument -> argument instanceof Map)
        .map(argument -> ((Map<String, String>) argument))
        .filter(m -> m.get(CoreInputTypes.SORT_FIELD_FIELD) != null)
        .findFirst();
    if (!map.isPresent()) {
      throw ExceptionHelper.illegalArgumentException(
          "Sort argument '{}' list should contain a map with the '{}' field.", sortArgument,
          CoreInputTypes.SORT_FIELD_FIELD);
    }
    return map.get()
        .get(CoreInputTypes.SORT_FIELD_FIELD);
  }

  private boolean containsSortFieldField(GraphQLArgument argument) {
    return CoreInputTypes.SORT_FIELD.equals(argument.getType()
        .getName());
  }

  private boolean isSortField(GraphQLArgument argument) {
    return CoreInputTypes.SORT_FIELD.equals(getTypeName(argument.getDefinition()
        .getType()));
  }
}
