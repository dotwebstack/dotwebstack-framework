package org.dotwebstack.framework.backend.rdf4j.directives;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.dotwebstack.framework.core.directives.CoreDirectives.TRANSFORM_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateDirectiveWiringTest {

  private static final Class<AssertionError> ASSERTION_ERROR = AssertionError.class;

  private AggregateDirectiveWiring aggregateDirectiveWiring;

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinition;

  @Mock
  private GraphQLFieldsContainer graphQlFieldsContainer;

  @Mock
  private NodeShapeRegistry nodeShapeRegistry;

  @BeforeEach
  void doBefore() {
    aggregateDirectiveWiring = new AggregateDirectiveWiring(nodeShapeRegistry);
  }

  @Nested
  class OnField {

    @Mock
    private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

    @Test
    void withValidScalar() {
      // Arrange
      when(environment.getFieldDefinition()).thenReturn(graphQlFieldDefinition);
      when(environment.getFieldsContainer()).thenReturn(graphQlFieldsContainer);
      when(environment.getElement()).thenReturn(mock(GraphQLFieldDefinition.class));
      setupForSuccess();

      // Act & Assert
      assertDoesNotThrow(() -> aggregateDirectiveWiring.onField(environment));
    }
  }

  @Nested
  class OnInputObjectField {

    @Mock
    private SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment;

    @Test
    void withValidScalar() {
      // Arrange
      when(environment.getFieldDefinition()).thenReturn(graphQlFieldDefinition);
      when(environment.getFieldsContainer()).thenReturn(graphQlFieldsContainer);
      when(environment.getElement()).thenReturn(mock(GraphQLInputObjectField.class));
      setupForSuccess();

      // Act & Assert
      assertDoesNotThrow(() -> aggregateDirectiveWiring.onInputObjectField(environment));
    }
  }

  private void setupForSuccess() {
    when(graphQlFieldsContainer.getName()).thenReturn("Brewery");

    when(graphQlFieldDefinition.getType()).thenReturn(GraphQLList.list(GraphQLString));
    when(graphQlFieldDefinition.getDirective(TRANSFORM_NAME)).thenReturn(mock(GraphQLDirective.class));

    NodeShape nodeShapeMock = mock(NodeShape.class);
    PropertyShape propertyShapeMock = mock(PropertyShape.class);
    when(nodeShapeMock.getPropertyShape(any())).thenReturn(propertyShapeMock);
    when(propertyShapeMock.getMaxCount()).thenReturn(0);
    when(nodeShapeRegistry.getByShaclName(any())).thenReturn(nodeShapeMock);
  }


  @Nested
  @DisplayName("Validate methods")
  class ValidateMethods {

    @Nested
    @DisplayName("Data Type")
    class DataType {

      @Test
      void whenHasTransformDirectiveAndTypeInt() {
        // Arrange
        setUp(GraphQLInt, mock(GraphQLDirective.class));

        // Act & Assert
        assertDoesNotThrow(() -> aggregateDirectiveWiring.validateDataType(graphQlFieldDefinition));
      }

      @Test
      void whenNotHasTransformDirectiveAndTypeNotInt() {
        // Arrange
        setUp(GraphQLString, null);

        // Act & Assert
        assertThrows(ASSERTION_ERROR, () -> aggregateDirectiveWiring.validateDataType(graphQlFieldDefinition));
      }

      @Test
      void whenHasTypeStringAndTransformDirective() {
        // Arrange
        setUp(GraphQLString, mock(GraphQLDirective.class));

        // Act & Assert
        assertDoesNotThrow(() -> aggregateDirectiveWiring.validateDataType(graphQlFieldDefinition));
      }

      private void setUp(GraphQLScalarType scalarType, GraphQLDirective directive) {
        when(graphQlFieldDefinition.getType()).thenReturn(scalarType);
        when(graphQlFieldDefinition.getDirective(TRANSFORM_NAME)).thenReturn(directive);
      }
    }

    @Nested
    @DisplayName("Max Count")
    class MaxCount {

      @Mock
      private NodeShape nodeShape;

      @Mock
      private PropertyShape propertyShape;

      @Test
      void whenMaxCountZero() {
        // Arrange
        setUp(0);

        // Act & Assert
        assertDoesNotThrow(() -> aggregateDirectiveWiring.validateMax("Brewery", graphQlFieldDefinition));
      }

      @Test
      void whenMaxCountOne() {
        // Arrange
        setUp(1);

        // Act & Assert
        assertThrows(ASSERTION_ERROR, () -> aggregateDirectiveWiring.validateMax("Brewery", graphQlFieldDefinition));
      }

      @Test
      void whenMaxCountTwo() {
        // Arrange
        setUp(2);

        // Act & Assert
        assertDoesNotThrow(() -> aggregateDirectiveWiring.validateMax("Brewery", graphQlFieldDefinition));
      }

      private void setUp(int i) {
        when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
        when(propertyShape.getMaxCount()).thenReturn(i);
        when(nodeShapeRegistry.getByShaclName(any())).thenReturn(nodeShape);
      }
    }
  }

}
