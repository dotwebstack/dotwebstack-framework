package org.dotwebstack.framework.core.arguments;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreInputTypes;

public class ArgumentValidator extends ArgumentTraverser {

  private final SortFieldValidator sortFieldValidator;

  public ArgumentValidator(@NonNull SortFieldValidator inputValueValidator) {
    this.sortFieldValidator = inputValueValidator;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onArgument(GraphQLType fieldDefinitionType, GraphQLArgument argument, Object value) {
    GraphQLInputType type = argument.getType();
    if (isSortField(argument)) {
      if (type instanceof GraphQLList) {
        validateSortFieldList(fieldDefinitionType, value, type);
      } else {
        validateSortField(fieldDefinitionType, value);
      }
    }
  }

  @Override
  protected void onInputObjectField(GraphQLType fieldDefinitionType, GraphQLInputObjectField inputObjectField,
      Object value) {
    GraphQLInputType type = inputObjectField.getType();
    if (CoreInputTypes.SORT_FIELD.equals(getTypeName(inputObjectField.getType()))) {
      if (type instanceof GraphQLList) {
        validateSortFieldList(fieldDefinitionType, value, type);
      } else {
        validateSortField(fieldDefinitionType, value);
      }
    }
  }

  private void validateSortFieldList(GraphQLType fieldDefinitionType, Object value, GraphQLInputType type) {
    if (!(value instanceof List)) {
      ExceptionHelper.illegalArgumentException("Sort field type '{}' should be a List.", type);
    }
    List<?> valueList = (List) value;
    valueList.stream()
        .forEach(sortFieldValue -> validateSortField(fieldDefinitionType, sortFieldValue));
  }

  private void validateSortField(GraphQLType fieldDefinitionType, Object value) {
    Optional<String> sortFieldValue = getSortFieldValue(value);
    if (!sortFieldValue.isPresent()) {
      ExceptionHelper.illegalArgumentException("Sort field '{}' should contain '{}' field value.",
          fieldDefinitionType.getName(), CoreInputTypes.SORT_FIELD_FIELD);
    }
    this.sortFieldValidator.validateSortFieldValue(getTypeName(fieldDefinitionType), sortFieldValue.get());
  }

  @SuppressWarnings("unchecked")
  private Optional<String> getSortFieldValue(Object sortArgument) {
    if (sortArgument == null) {
      return Optional.empty();
    } else if (!(sortArgument instanceof Map)) {
      throw ExceptionHelper.illegalArgumentException("Sort argument '{}' should be a map.", sortArgument);
    } else {
      return Optional.of((String) ((Map) sortArgument).get(CoreInputTypes.SORT_FIELD_FIELD));
    }
  }

  private boolean isSortField(GraphQLArgument argument) {
    return CoreInputTypes.SORT_FIELD.equals(getTypeName(argument.getDefinition()
        .getType()));
  }
}
