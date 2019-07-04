package org.dotwebstack.framework.core.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
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
      throw illegalArgumentException("Sort field type '{}' should be a List.", type);
    }
    List<?> valueList = (List) value;
    valueList.forEach(sortFieldValue -> validateSortField(fieldDefinitionType, sortFieldValue));
  }

  private void validateSortField(GraphQLType fieldDefinitionType, Object value) {
    Optional<String> sortFieldValue = getSortFieldValue(value);
    if (!sortFieldValue.isPresent()) {
      throw illegalArgumentException("Sort field '{}' should contain '{}' field value.", fieldDefinitionType.getName(),
          CoreInputTypes.SORT_FIELD_FIELD);
    }
    this.validateSortFieldValue(getTypeName(fieldDefinitionType), null, null, sortFieldValue.get());
  }

  private Optional<String> getSortFieldValue(Object sortArgument) {
    if (sortArgument == null) {
      return Optional.empty();
    } else if (!(sortArgument instanceof Map)) {
      throw illegalArgumentException("Sort argument '{}' should be a map.", sortArgument);
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

  public void validateSortFieldValue(String type, String parentField, String parentType, String fieldPath) {
    String[] fields = fieldPath.split("\\.");
    String field = fields[0];
    TypeDefinition<?> typeDef = typeDefinitionRegistry.getType(type)
        .orElseThrow(
            () -> invalidConfigurationException("Type '{}' not found in sort field path '{}'.", type, fieldPath));

    if (typeDef instanceof ObjectTypeDefinition) {
      Optional<FieldDefinition> matchedDefinition = ((ObjectTypeDefinition) typeDef).getFieldDefinitions()
          .stream()
          .filter(fieldDefinition -> fieldDefinition.getName()
              .equals(field))
          .findFirst();

      if (!matchedDefinition.isPresent()) {
        throw invalidConfigurationException("Type '{}' has no Field '{}' for sort field path '{}'.", type, field,
            fieldPath);
      }

      Type<?> matchedType = matchedDefinition.get()
          .getType();
      if (hasListType(matchedType)) {
        throw invalidConfigurationException(
            "Type '{}' of Field '{}' used in sort field path '{}' is a List, which is not allowed for sorting.", type,
            field, fieldPath);
      }

      if (fields.length > 1) {
        validateSortFieldValue(getTypeName(matchedType), field, typeDef.getName(),
            String.join(".", ArrayUtils.removeElement(fields, field)));
      }
    } else {
      throw invalidConfigurationException(
          "Field '{}' on Type '{}' is required to be an object type, but was of type '{}'.", parentField, parentType,
          type);
    }
  }
}
