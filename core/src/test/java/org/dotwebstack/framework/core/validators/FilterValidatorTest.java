package org.dotwebstack.framework.core.validators;

import static graphql.Scalars.GraphQLString;
import static graphql.language.TypeName.newTypeName;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.GraphQLArgument;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterValidatorTest {

  private FilterValidator validator = new FilterValidator(new CoreTraverser());

  @Mock
  private TypeDefinitionRegistry typeDefinitionRegistry;

  @Mock
  private ObjectTypeDefinition typeDefinition;

  @Test
  void validate_ExistingValue_optionalFieldArgument() {
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
        .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertDoesNotThrow(() -> validator.checkField(fieldArgument("sinceBefore"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_nonExistingValue_optionalFieldArgument() {
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
        .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertThrows(DirectiveValidationException.class,
        () -> validator.checkField(fieldArgument("page"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_NoValues_optionalFieldArgument() {
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
        .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertDoesNotThrow(() -> validator.checkField(fieldArgument("founded"), typeDefinitionRegistry, "", "Brewery"));
  }

  @Test
  void validate_noValueNonexistingQueryArgumentName_optionalFieldArgument() {
    // Assign
    ObjectTypeDefinition objectTypeDefinition = createBreweryDefinition();
    when(typeDefinitionRegistry.getType("Brewery", ObjectTypeDefinition.class))
        .thenReturn(Optional.of(objectTypeDefinition));

    // Act & Assert
    assertThrows(DirectiveValidationException.class,
        () -> validator.checkField(fieldArgument(null), typeDefinitionRegistry, "sinceAfter", "Brewery"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"=", "!=", "<", "<=", ">", ">="})
  void checkOperator_DoesNotThrowException_ForValidOperator(String operatorValue) {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .value(operatorValue)
        .type(Scalars.GraphQLString)
        .build();

    // Act
    assertDoesNotThrow(() -> validator.checkOperator(argument, "founded"));
  }

  @Test
  void checkOperator_ThrowsException_ForInvalidOperator() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .value("?")
        .type(Scalars.GraphQLString)
        .build();

    // Act && Assert that exception is thrown
    assertThrows(DirectiveValidationException.class, () -> validator.checkOperator(argument, "operator"));
  }

  @Test
  void checkOperator_DoesNotThrowException_ForMissingOperatorValue() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .type(Scalars.GraphQLString)
        .build();

    // Act
    assertDoesNotThrow(() -> validator.checkOperator(argument, "operator"));
  }


  // For testpurposes a graphQl schema is being build below
  private GraphQLArgument fieldArgument(String fieldName) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.FILTER_ARG_FIELD)
        .type(GraphQLString)
        .value(fieldName)
        .build();
  }

  private ObjectTypeDefinition createBreweryDefinition() {
    List<FieldDefinition> propertyList = new ArrayList<>();

    propertyList.add(FieldDefinition.newFieldDefinition()
        .name("founded")
        .type(newTypeName(GraphQLString.getName()).build())
        .build());

    propertyList.add(FieldDefinition.newFieldDefinition()
        .name("sinceBefore")
        .type(newTypeName(GraphQLString.getName()).build())
        .build());

    return ObjectTypeDefinition.newObjectTypeDefinition()
        .name("Brewery")
        .fieldDefinitions(propertyList)
        .build();
  }
}
