package org.dotwebstack.framework.core.directives;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNonNull;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConstraintDirectiveWiringTest {

  private ConstraintDirectiveWiring constraintDirectiveWiring;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLArgument> argumentEnvironment;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> inputObjectFieldEnvironment;

  @Mock
  private ConstraintValidator constraintValidator;

  @BeforeEach
  void doBefore() {
    constraintDirectiveWiring = new ConstraintDirectiveWiring(constraintValidator);
  }

  @Test
  void onArgument_returnsNull_forGivenArgument() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_MIN)
        .type(new GraphQLNonNull(Scalars.GraphQLInt))
        .value(1)
        .build();

    when(argumentEnvironment.getElement()).thenReturn(argument);

    // Act
    constraintDirectiveWiring.onArgument(argumentEnvironment);

    // Assert
    verify(constraintValidator).validateSchema(DirectiveContainerTuple.builder()
        .container(argument)
        .value(argument.getDefaultValue())
        .build());
  }

  @Test
  void onArgument_throwsException_forGivenArgument() {
    // Act
    when(argumentEnvironment.getElement()).thenThrow(new DirectiveValidationException("boom!"));

    // Assert
    Assertions.assertThrows(InvalidConfigurationException.class,
        () -> constraintDirectiveWiring.onArgument(argumentEnvironment));
  }

  @Test
  void onInputObjectField_returnsNull_forGivenArgument() {
    // Arrange
    GraphQLInputObjectField field = GraphQLInputObjectField.newInputObjectField()
        .name("input")
        .type(Scalars.GraphQLInt)
        .build();

    when(inputObjectFieldEnvironment.getElement()).thenReturn(field);

    // Act
    constraintDirectiveWiring.onInputObjectField(inputObjectFieldEnvironment);

    // Assert
    verify(constraintValidator).validateSchema(DirectiveContainerTuple.builder()
        .container(field)
        .value(field.getDefaultValue())
        .build());
  }


  @Test
  void onInputObjectField_throwsException_forGivenArgument() {
    // Act
    when(inputObjectFieldEnvironment.getElement()).thenThrow(new DirectiveValidationException("boom!"));

    // Assert
    Assertions.assertThrows(InvalidConfigurationException.class,
        () -> constraintDirectiveWiring.onInputObjectField(inputObjectFieldEnvironment));
  }
}
