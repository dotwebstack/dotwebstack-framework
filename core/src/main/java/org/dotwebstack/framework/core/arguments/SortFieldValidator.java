package org.dotwebstack.framework.core.arguments;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;
import static org.dotwebstack.framework.core.helpers.TypeHelper.hasListType;

import com.google.common.collect.ImmutableMap;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreInputTypes;

public class SortFieldValidator {

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public SortFieldValidator(@NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  private void validateArgument(GraphQLType fieldDefinitionType, GraphQLArgument argument, Object value) {
    GraphQLInputType type = argument.getType();
    if (isSortField(argument)) {
      if (type instanceof GraphQLList) {
        validateSortFieldList(fieldDefinitionType, value, type);
      } else {
        validateSortField(fieldDefinitionType, value);
      }
    }
  }

  private void validateInputObjectField(GraphQLType fieldDefinitionType, GraphQLInputObjectField inputObjectField,
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
      throw ExceptionHelper.illegalArgumentException("Sort field type '{}' should be a List.", type);
    }
    List<?> valueList = (List) value;
    valueList.stream()
        .forEach(sortFieldValue -> validateSortField(fieldDefinitionType, sortFieldValue));
  }

  private void validateSortField(GraphQLType fieldDefinitionType, Object value) {
    Optional<String> sortFieldValue = getSortFieldValue(value);
    if (!sortFieldValue.isPresent()) {
      throw ExceptionHelper.illegalArgumentException("Sort field '{}' should contain '{}' field value.",
          fieldDefinitionType.getName(), CoreInputTypes.SORT_FIELD_FIELD);
    }
    this.validateSortFieldValue(getTypeName(fieldDefinitionType), sortFieldValue.get());
  }

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

  public void traverse(@NonNull DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> arguments = dataFetchingEnvironment.getArguments();

    fieldDefinition.getArguments()
        .stream()
        .forEach(argument -> {
          Object value = arguments.get(argument.getName());
          if (argument.getType() instanceof GraphQLInputObjectType) {
            traverseInputObjectType(fieldDefinition.getType(), (GraphQLInputObjectType) argument.getType(),
                value != null ? castToMap(value) : ImmutableMap.of());
          } else {
            traverseArgument(fieldDefinition.getType(), argument, value);
          }
        });
  }

  public void traverseArgument(GraphQLType fieldDefinitionType, GraphQLArgument argument, Object value) {
    validateArgument(fieldDefinitionType, argument, value);
    if (argument.getType() instanceof GraphQLInputObjectType) {
      traverseInputObjectType(fieldDefinitionType, (GraphQLInputObjectType) argument.getType(),
          value != null ? castToMap(value) : ImmutableMap.of());
    }
  }

  private void traverseInputObjectType(GraphQLType fieldDefinitionType, GraphQLInputObjectType graphQlInputObjectType,
      Map<String, Object> value) {
    graphQlInputObjectType.getFields()
        .forEach(fd -> traverseInputObjectField(fieldDefinitionType, fd, value.get(fd.getName())));
  }

  private void traverseInputObjectField(GraphQLType fieldDefinitionType, GraphQLInputObjectField inputObjectField,
      Object value) {
    validateInputObjectField(fieldDefinitionType, inputObjectField, value);
    if (inputObjectField.getType() instanceof GraphQLInputObjectType) {
      traverseInputObjectType(fieldDefinitionType, (GraphQLInputObjectType) inputObjectField.getType(),
          value != null ? castToMap(value) : ImmutableMap.of());
    }
  }

  void validateSortFieldValue(String typeName, String sortFieldValue) {
    List<String> fieldPath = Arrays.asList(sortFieldValue.split("\\."));

    String currentType = typeName;
    String parentField = "";
    String parentType = "";
    for (String field : fieldPath) {
      @SuppressWarnings("rawtypes")
      Optional<TypeDefinition> typeDef = typeDefinitionRegistry.getType(currentType);
      if (!typeDef.isPresent()) {
        throw ExceptionHelper.invalidConfigurationException("Type '{}' not found in sort field path '{}'.", currentType,
            fieldPath);
      }

      if (typeDef.get() instanceof ObjectTypeDefinition) {
        Optional<FieldDefinition> matchedDefinition = ((ObjectTypeDefinition) typeDef.get()).getFieldDefinitions()
            .stream()
            .filter(fieldDefinition -> fieldDefinition.getName()
                .equals(field))
            .findFirst();

        if (!matchedDefinition.isPresent()) {
          throw ExceptionHelper.invalidConfigurationException("Type '{}' has no Field '{}' for sort field path '{}'.",
              currentType, field, fieldPath);
        }

        Type<?> type = matchedDefinition.get()
            .getType();
        if (hasListType(type)) {
          throw ExceptionHelper.invalidConfigurationException(
              "Type '{}' of Field '{}' used in sort field path '{}' is a List, which is not allowed for sorting.", type,
              field, fieldPath);
        }
        parentField = field;
        parentType = currentType;
        currentType = getTypeName(type);
      } else {
        throw ExceptionHelper.invalidConfigurationException(
            "Field '{}' on Type '{}' is required to be an object type (since it is not used as a leaf in the sort "
                + "argument), but was of type '{}'.",
            parentField, parentType, currentType);
      }
    }
  }
}
