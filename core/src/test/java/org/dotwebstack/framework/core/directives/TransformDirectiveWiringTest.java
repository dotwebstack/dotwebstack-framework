package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveWiringTest {

  private static final String FIELD_NAME = "foo";

  private final TransformDirectiveWiring transformDirectiveWiring = new TransformDirectiveWiring();

  @TestFactory
  Stream<DynamicContainer> onField() {
    return Stream.of(dynamicContainer("Wraps existing Fetcher for ",
        Stream.of(getTestCase("scalar Field String with value", GraphQLString),
            getTestCase("scalar Field Int with value", GraphQLInt),
            getTestCase("non-null scalar String with value", nonNull(GraphQLString)),
            getTestCase("non-null scalar Int with value", nonNull(GraphQLInt)),
            getTestCase("list scalar Field with value", list(GraphQLString)),
            getTestCase("list non-null scalar Field with value", list(nonNull(GraphQLString))),
            getTestCase("non-null list scalar Field with value", nonNull(list(GraphQLString))),
            getTestCase("non-null list non-null scalar Field with value", nonNull(list(nonNull(GraphQLString)))),
            getExceptionTest("Throws exception for non scalar field"))));
  }

  private DynamicTest getTestCase(String name, GraphQLOutputType type) {
    // Arrange
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = getEnvironment();
    GraphQLFieldDefinition fieldDefinition = getGraphQlFieldDefinition(type);
    when(environment.getElement()).thenReturn(fieldDefinition);

    return dynamicTest(name, () -> {
      // Act
      GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

      // Assert
      assertThat(result, is(sameInstance(fieldDefinition)));
    });
  }

  @SuppressWarnings("unchecked")
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> getEnvironment() {
    return mock(SchemaDirectiveWiringEnvironment.class);
  }

  private GraphQLFieldDefinition getGraphQlFieldDefinition(GraphQLOutputType type) {
    return GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(type)
        .build();
  }

  private DynamicTest getExceptionTest(String name) {
    // Arrange
    SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment = getEnvironment();
    GraphQLOutputType type = GraphQLObjectType.newObject()
        .name(FIELD_NAME)
        .build();
    GraphQLFieldDefinition fieldDefinition = getGraphQlFieldDefinition(type);
    when(environment.getElement()).thenReturn(fieldDefinition);
    when(environment.getFieldsContainer()).thenReturn(mock(GraphQLFieldsContainer.class));

    return dynamicTest(name, () -> {
      // Act & Assert
      Exception expectedException =
          assertThrows(InvalidConfigurationException.class, () -> transformDirectiveWiring.onField(environment));

      // Assert
      assertThat(expectedException.getMessage(), containsString("can only be used with (a list of) scalar fields."));
    });
  }
}
