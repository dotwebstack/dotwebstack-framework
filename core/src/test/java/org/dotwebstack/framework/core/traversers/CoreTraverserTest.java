package org.dotwebstack.framework.core.traversers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoreTraverserTest {

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  private CoreTraverser coreTraverser = new CoreTraverser();

  @Test
  void getTuples_returnsList_ForScalarArgument() {

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("identifier")
        .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
        .build();

    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("brewery")
        .type(GraphQLObjectType.newObject()
            .name("Brewery"))
        .argument(argument)
        .build();

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(dataFetchingEnvironment.getArguments()).thenReturn(ImmutableMap.of("identifier", 1));

    assertThat(coreTraverser.getTuples(dataFetchingEnvironment, TraverserFilter.noFilter()))
        .contains(new DirectiveContainerTuple(argument, 1));
  }

  @Test
  void getTuples_returnsList_ForInputObjectField() {

    GraphQLInputObjectField field = GraphQLInputObjectField.newInputObjectField()
        .name("identifier")
        .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
        .build();

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("input")
        .type(GraphQLInputObjectType.newInputObject()
            .name("Input")
            .field(field)
            .build())
        .build();

    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("brewery")
        .type(GraphQLObjectType.newObject()
            .name("Brewery"))
        .argument(argument)
        .build();

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(dataFetchingEnvironment.getArguments()).thenReturn(ImmutableMap.of("input", ImmutableMap.of("identifier", 1)));

    assertThat(coreTraverser.getTuples(dataFetchingEnvironment, TraverserFilter.noFilter()))
        .contains(new DirectiveContainerTuple(field, 1));
  }


}
