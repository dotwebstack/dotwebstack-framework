package org.dotwebstack.framework.core.validation;

import graphql.language.ArrayValue;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreInputTypes;

public class TypeDefinitionValidator {

  private TypeDefinitionRegistry typeDefinitionRegistry;

  public TypeDefinitionValidator(@NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  public void validate() {
    this.typeDefinitionRegistry.types()
        .values()
        .stream()
        .filter(td -> td instanceof ObjectTypeDefinition)
        .map(td -> (ObjectTypeDefinition) td)
        .forEach(this::validateObjectTypeDefinitions);
  }

  private void validateObjectTypeDefinitions(ObjectTypeDefinition queryDefinition) {
    queryDefinition.getFieldDefinitions()
        .forEach(this::validateFieldDefinition);
  }

  private void validateFieldDefinition(FieldDefinition fieldDefinition) {
    fieldDefinition.getInputValueDefinitions()
        .forEach(inputValueDef -> validateInputFieldDefinition(fieldDefinition, inputValueDef));
  }

  private void validateInputFieldDefinition(FieldDefinition fieldDefinition,
      InputValueDefinition inputValueDefinition) {
    if (CoreInputTypes.SORT_FIELD.equals(getTypeName(inputValueDefinition.getType()))) {
      validateSortFieldInputFieldDefinition(fieldDefinition, inputValueDefinition);
    }
  }

  private void validateSortFieldInputFieldDefinition(FieldDefinition fieldDefinition,
      InputValueDefinition inputValueDefinition) {
    String typeName = getTypeName(fieldDefinition.getType());

    ArrayValue arrayValue = (ArrayValue) inputValueDefinition.getDefaultValue();
    List<ObjectField> objectFields = new ArrayList<>();
    arrayValue.getValues()
        .stream()
        .filter(v -> v instanceof ObjectValue)
        .forEach(v -> objectFields.addAll(((ObjectValue) v).getObjectFields()));
    Optional<ObjectField> field = objectFields.stream()
        .filter(of -> of.getName()
            .equals(CoreInputTypes.SORT_FIELD_FIELD))
        .findFirst();
    ObjectField objectField = field.orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
        "Field '{}' not found for sort field inputValueDefinition '{}'", CoreInputTypes.SORT_FIELD_FIELD,
        inputValueDefinition));
    validateSortFieldDefault(typeName, objectField.getValue());
  }

  private void validateSortFieldDefault(String typeName, Value<?> defaultValue) {
    String stringValue = ((StringValue) defaultValue).getValue();
    List<String> fieldPath = Arrays.asList(stringValue.split("\\."));

    String currentType = typeName;
    for (String field : fieldPath) {
      @SuppressWarnings("rawtypes")
      Optional<TypeDefinition> typeDef = this.typeDefinitionRegistry.getType(currentType);
      if (!typeDef.isPresent()) {
        throw ExceptionHelper.invalidConfigurationException("Type '{}' not found in sort field path '{}'.", currentType,
            fieldPath);
      }
      ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDef.get();
      Optional<FieldDefinition> matchedDefinition = objectTypeDefinition.getFieldDefinitions()
          .stream()
          .filter(fd -> fd.getName()
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
      currentType = getTypeName(type);
    }
  }

  private boolean hasListType(Type<?> type) {
    if (type instanceof NonNullType) {
      return hasListType(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return true;
    } else if (type instanceof TypeName) {
      return false;
    } else {
      throw new InvalidConfigurationException("unsupported type: " + type.getClass());
    }
  }

  private String getTypeName(Type<?> type) {
    if (type instanceof NonNullType) {
      return getTypeName(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return getTypeName(((ListType) type).getType());
    } else if (type instanceof TypeName) {
      return ((TypeName) type).getName();
    } else {
      throw new InvalidConfigurationException("unsupported type: " + type.getClass());
    }
  }
}
