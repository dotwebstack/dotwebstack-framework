package org.dotwebstack.framework.core.validation;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;
import static org.dotwebstack.framework.core.helpers.TypeHelper.hasListType;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class SortFieldValidator implements SchemaDirectiveWiring {

  private TypeDefinitionRegistry typeDefinitionRegistry;

  public SortFieldValidator(TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  public void validateSortFieldValue(String typeName, String sortFieldvalue) {
    List<String> fieldPath = Arrays.asList(sortFieldvalue.split("\\."));

    String currentType = typeName;
    for (String field : fieldPath) {
      @SuppressWarnings("rawtypes")
      Optional<TypeDefinition> typeDef = typeDefinitionRegistry.getType(currentType);
      if (!typeDef.isPresent()) {
        throw ExceptionHelper.invalidConfigurationException("Type '{}' not found in sort field path '{}'.", currentType,
            fieldPath);
      }
      ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDef.get();
      Optional<FieldDefinition> matchedDefinition = objectTypeDefinition.getFieldDefinitions()
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
      currentType = getTypeName(type);
    }
  }
}
