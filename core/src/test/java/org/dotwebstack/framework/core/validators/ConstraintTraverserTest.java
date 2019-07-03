package org.dotwebstack.framework.core.validators;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNonNull;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.traversers.ConstraintTraverser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConstraintTraverserTest {

  @Mock
  private ConstraintValidator constraintValidator;

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private GraphQLDirective directive;

  @BeforeEach
  void doBefore() {
    constraintValidator = new ConstraintValidator(new ConstraintTraverser());
  }

  @Test
  void traverse() {
    when(directive.getName()).thenReturn(CoreDirectives.CONSTRAINT_NAME);

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("argfield")
        .type(GraphQLInt)
        .value(1)
        .withDirective(directive)
        .build();

    GraphQLArgument inputArgument = GraphQLArgument.newArgument()
        .name("input")
        .type(GraphQLInputObjectType.newInputObject()
            .name("input")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name("field")
                .type(GraphQLString)
                .defaultValue(1)
                .withDirective(directive))
            .build())
        .build();

    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("query")
        .type(GraphQLNonNull.nonNull(GraphQLString))
        .argument(argument)
        .argument(inputArgument)
        .build();

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    assertDoesNotThrow(() -> constraintValidator.validateDataFetchingEnvironment(dataFetchingEnvironment));
  }
}
