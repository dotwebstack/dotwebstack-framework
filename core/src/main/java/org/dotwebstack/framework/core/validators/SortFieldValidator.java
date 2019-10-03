package org.dotwebstack.framework.core.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;
import static org.dotwebstack.framework.core.helpers.TypeHelper.hasListType;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.noFilter;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLInputObjectField;
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
import org.dotwebstack.framework.core.traversers.CoreTraverser;

public class SortFieldValidator implements QueryValidator {

  private final CoreTraverser coreTraverser;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public SortFieldValidator(@NonNull CoreTraverser coreTraverser,
      @NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.coreTraverser = coreTraverser;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  public void validate(@NonNull DataFetchingEnvironment dataFetchingEnvironment) {
    coreTraverser.getTuples(dataFetchingEnvironment, noFilter())
        .forEach(
            tuple -> validate(dataFetchingEnvironment.getFieldDefinition(), tuple.getContainer(), tuple.getValue()));
  }

  public void validate(GraphQLType fieldDefinitionType, GraphQLDirectiveContainer directiveContainer, Object value) {
    if (isSortField(getInputValueDefinition(directiveContainer))) {
      GraphQLInputType inputType = getInputType(directiveContainer);
      if (inputType instanceof GraphQLList) {
        validateSortFieldList(fieldDefinitionType, value, inputType);
      } else {
        validateSortField(fieldDefinitionType, value);
      }
    }
  }

  private InputValueDefinition getInputValueDefinition(GraphQLDirectiveContainer directiveContainer) {
    if (directiveContainer instanceof GraphQLArgument) {
      return ((GraphQLArgument) directiveContainer).getDefinition();
    } else if (directiveContainer instanceof GraphQLInputObjectField) {
      return ((GraphQLInputObjectField) directiveContainer).getDefinition();
    }
    throw unsupportedOperationException("Unable to get inputValueDefinition for class {}", directiveContainer.getClass()
        .getSimpleName());
  }

  private GraphQLInputType getInputType(GraphQLDirectiveContainer directiveContainer) {
    if (directiveContainer instanceof GraphQLArgument) {
      return ((GraphQLArgument) directiveContainer).getType();
    } else if (directiveContainer instanceof GraphQLInputObjectField) {
      return ((GraphQLInputObjectField) directiveContainer).getType();
    }
    throw unsupportedOperationException("Unable to get inputType for class {}", directiveContainer.getClass()
        .getSimpleName());
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
    if (sortFieldValue.isEmpty()) {
      throw illegalArgumentException("Sort field '{}' should contain '{}' field value.", fieldDefinitionType.getName(),
          CoreInputTypes.SORT_FIELD_FIELD);
    }
    this.validateSortFieldValue(getTypeName(fieldDefinitionType), null, null, sortFieldValue.get());
  }

  private Optional<String> getSortFieldValue(Object sortArgument) {
    if (sortArgument == null) {
      return Optional.empty();
    } else if (!(sortArgument instanceof Map)) {
      throw illegalArgumentException("Sort container '{}' should be a map.", sortArgument);
    } else {
      return Optional.of((String) ((Map) sortArgument).get(CoreInputTypes.SORT_FIELD_FIELD));
    }
  }

  private boolean isSortField(InputValueDefinition inputValueDefinition) {
    return CoreInputTypes.SORT_FIELD.equals(getTypeName(inputValueDefinition.getType()));
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

      if (matchedDefinition.isEmpty()) {
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
