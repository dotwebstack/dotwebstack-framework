package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.GraphQLArgument;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Collections;
import java.util.Optional;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.directives.FilterDirectiveTraverser;
import org.dotwebstack.framework.core.directives.FilterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterValidatorTest {

  private static FilterValidator validator = new FilterValidator(new FilterDirectiveTraverser());

  @Mock
  private ObjectTypeDefinition typeDefinition;

  @Mock
  private TypeDefinitionRegistry registry;

  @Test
  void checkField_DoesNotThrowException_ForValidField() {
    // Arrange
    when(registry.getType("Brewery", ObjectTypeDefinition.class)).thenReturn(Optional.of(typeDefinition));
    when(typeDefinition.getFieldDefinitions())
        .thenReturn(Collections.singletonList(new FieldDefinition("founded", null)));

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("field")
        .value("founded")
        .type(Scalars.GraphQLString)
        .build();

    // Act & Assert
    assertDoesNotThrow(() -> validator.checkField(argument, registry, "founded", "Brewery"));
  }

  @Test
  void checkField_ThrowsException_ForInvalidField() {
    // Arrange
    when(registry.getType("Brewery", ObjectTypeDefinition.class)).thenReturn(Optional.of(typeDefinition));
    when(typeDefinition.getFieldDefinitions())
        .thenReturn(Collections.singletonList(new FieldDefinition("founded", null)));

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("field")
        .value("foo")
        .type(Scalars.GraphQLString)
        .build();

    // Act && Assert that exception is thrown
    assertThrows(DirectiveValidationException.class,
        () -> validator.checkField(argument, registry, "founded", "Brewery"));
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
}
