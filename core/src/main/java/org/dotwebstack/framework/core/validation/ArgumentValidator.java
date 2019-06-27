package org.dotwebstack.framework.core.validation;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.language.ArrayValue;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreInputTypes;

public class ArgumentValidator {

  private final SortFieldValidator sortFieldValidator;

  public ArgumentValidator(@NonNull SortFieldValidator inputValueValidator) {
    this.sortFieldValidator = inputValueValidator;
  }

  public void validateArguments(@NonNull DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> arguments = dataFetchingEnvironment.getArguments();

    fieldDefinition.getArguments()
        .stream()
        .forEach(argument -> {
          if (argument.getType() instanceof GraphQLInputObjectType) {
            onInputObjectType(fieldDefinition, argument, (GraphQLInputObjectType) argument.getType(), arguments);
          } else {
            onArgument(fieldDefinition, argument, arguments);
          }
        });
  }

  private void onArgument(GraphQLFieldDefinition fieldDefinition, GraphQLArgument argument,
                          Map<String, Object> arguments) {
    if (isSortField(argument)) {
      List<String> sortFieldValues = getSortFieldValues(arguments.get(argument.getName()));
      validateSortFieldArgument((ArrayValue) argument.getValue(), fieldDefinition, sortFieldValues);
    }
  }

  private void onInputObjectType(GraphQLFieldDefinition fieldDefinition, GraphQLArgument argument,
                                 GraphQLInputObjectType graphQlInputObjectType, Map<String, Object> arguments) {
    graphQlInputObjectType.getDefinition()
        .getInputValueDefinitions()
        .stream()
        .filter(ivd -> CoreInputTypes.SORT_FIELD.equals(getTypeName(ivd.getType())))
        .forEach(ivd -> validateSortInputValueDefinition(fieldDefinition, argument, ivd, arguments));
  }

  @SuppressWarnings("unchecked")
  private void validateSortInputValueDefinition(GraphQLFieldDefinition fieldDefinition, GraphQLArgument argument,
                                                InputValueDefinition inputValueDefinition, Map<String, Object> arguments) {
    Map<String, Object> sortArguments = (Map<String, Object>) arguments.get(argument.getName());
    Object sortFieldArgument = sortArguments != null ? sortArguments.get(inputValueDefinition.getName()) : null;
    validateSortFieldArgument((ArrayValue) inputValueDefinition.getDefaultValue(), fieldDefinition,
        getSortFieldValues(sortFieldArgument));
  }

  private void validateSortFieldArgument(ArrayValue sortFieldArguments, GraphQLFieldDefinition fieldDefinition,
                                         List<String> sortFieldValues) {
    if (!sortFieldValues.isEmpty()) {
      sortFieldValues.forEach(sortFieldValue -> this.sortFieldValidator
          .validateSortFieldValue(getTypeName(fieldDefinition.getType()), sortFieldValue));
    }
  //todo:remove?
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
      String sortFieldValue = objectField.getValue()
          .toString();

      this.sortFieldValidator.validateSortFieldValue(getTypeName(fieldDefinition.getType()),
          sortFieldValue);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> getSortFieldValues(Object sortArgument) {
    if (sortArgument == null) {
      return Collections.emptyList();
    } else if (!(sortArgument instanceof List)) {
      throw ExceptionHelper.illegalArgumentException("Sort argument '{}' should be a list.", sortArgument);
    }
    return ((List<?>) sortArgument).stream()
        .map(argument -> (((Map<String, String>) argument)).get(CoreInputTypes.SORT_FIELD_FIELD))
        .collect(Collectors.toList());
  }

  private boolean containsSortFieldField(GraphQLArgument argument) {
    return CoreInputTypes.SORT_FIELD.equals(getTypeName(argument.getType()));
  }

  private boolean isSortField(GraphQLArgument argument) {
    return CoreInputTypes.SORT_FIELD.equals(getTypeName(argument.getDefinition()
        .getType()));
  }

  private String getStringValue(Value<?> value) {
    if (value instanceof StringValue) {
      return ((StringValue) value).getValue();
    } else {
      return value.toString();
    }
  }
}
