package org.dotwebstack.framework.core.directives;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.traversers.DirectiveArgumentTuple;
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

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_MIN)
        .type(Scalars.GraphQLInt)
        .value(1)
        .build();

    when(argumentEnvironment.getElement()).thenReturn(argument);

    constraintDirectiveWiring.onArgument(argumentEnvironment);

    verify(constraintValidator).validate(new DirectiveArgumentTuple(argument,argument.getDefaultValue()));
  }

  @Test
  void onArgument_throwsException_forGivenArgument() {

    when(argumentEnvironment.getElement()).thenThrow(new DirectiveValidationException("boom!"));

    Assertions.assertThrows(InvalidConfigurationException.class,
        () -> constraintDirectiveWiring.onArgument(argumentEnvironment));
  }

  @Test
  void onInputObjectField_returnsNull_forGivenArgument() {
    GraphQLInputObjectField field = GraphQLInputObjectField.newInputObjectField()
        .name("input")
        .type(Scalars.GraphQLInt)
        .build();

    when(inputObjectFieldEnvironment.getElement()).thenReturn(field);

    constraintDirectiveWiring.onInputObjectField(inputObjectFieldEnvironment);

    verify(constraintValidator).validate(new DirectiveArgumentTuple(field,field.getDefaultValue()));
  }


  @Test
  void onInputObjectField_throwsException_forGivenArgument() {
    when(inputObjectFieldEnvironment.getElement()).thenThrow(new DirectiveValidationException("boom!"));

    Assertions.assertThrows(InvalidConfigurationException.class,
        () -> constraintDirectiveWiring.onInputObjectField(inputObjectFieldEnvironment));
  }
}
