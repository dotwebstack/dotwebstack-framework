package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNonNull;
import java.util.Collections;
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

  private ConstraintTraverser constraintTraverser;

  @BeforeEach
  void doBefore() {
    constraintTraverser = new ConstraintTraverser(constraintValidator);
  }

  @Test
  void traverse() {
    GraphQLArgument directiveArgument = GraphQLArgument.newArgument()
            .name(CoreDirectives.CONSTRAINT_ARG_MAX)
            .type(GraphQLInt)
            .value(10)
            .build();

    when(directive.getName()).thenReturn(CoreDirectives.CONSTRAINT_NAME);
    when(directive.getArguments()).thenReturn(
            Collections.singletonList(directiveArgument));

    GraphQLArgument argument = GraphQLArgument.newArgument()
                    .name("argfield")
                    .type(GraphQLInt)
                    .value(1)
                    .withDirective(directive)
                    .build();

    GraphQLArgument inputArgument = GraphQLArgument.newArgument().name("input").type(
        GraphQLInputObjectType.newInputObject().name("input")
                .field(GraphQLInputObjectField
                        .newInputObjectField()
                        .name("field")
                        .type(GraphQLString)
                        .defaultValue(1)
                        .withDirective(directive)).build())
            .build();

    GraphQLFieldDefinition fieldDefinition =
            GraphQLFieldDefinition.newFieldDefinition().name("query")
                    .type(GraphQLNonNull.nonNull(GraphQLString))
                    .argument(argument).argument(inputArgument).build();

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    constraintTraverser.traverse(dataFetchingEnvironment);

    verify(constraintValidator).validate(directiveArgument,"field",1);


  }
}