package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLString;
import static graphql.language.TypeName.newTypeName;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.GraphQLArgument;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterValidatorTest {

  private FilterValidator validator = new FilterValidator(new FilterDirectiveTraverser());

  @Mock
  private TypeDefinitionRegistry typeDefinitionRegistry;

  @Test
  void validate_ExistingValue_optionalFieldArgument(){
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
      .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertDoesNotThrow(() ->
      validator.checkField(fieldArgument("founded"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_nonExistingValue_optionalFieldArgument(){
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
      .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertThrows(DirectiveValidationException.class,
      () -> validator.checkField(fieldArgument("page"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_NoValues_optionalFieldArgument(){
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
      .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertDoesNotThrow(() ->
      validator.checkField(fieldArgument("founded"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_noValueNonexistingQueryArgumentName_optionalFieldArgument(){
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
      .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertThrows(DirectiveValidationException.class,
      () -> validator.checkField(fieldArgument(null), typeDefinitionRegistry, "sinceAfter", "Brewery"));
  }

  private GraphQLArgument fieldArgument(String fieldName) {
    return GraphQLArgument.newArgument()
      .name(CoreDirectives.FILTER_ARG_FIELD)
      .type(GraphQLString)
      .value(fieldName)
      .build();
  }

  private ObjectTypeDefinition createBreweryDefinition(){
    return ObjectTypeDefinition.newObjectTypeDefinition()
      .name("Brewery")
      .fieldDefinition(
        FieldDefinition
          .newFieldDefinition()
          .name("founded")
          .type(newTypeName(GraphQLString.getName()).build())
          .build()
      )
      .build();
  }
}
