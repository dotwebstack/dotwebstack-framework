package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceDirectiveWiringTest {

  private static final Class<AssertionError> ASSERTION_ERROR = AssertionError.class;

  private ResourceDirectiveWiring resourceDirectiveWiring;

  @BeforeEach
  void setUp() {
    resourceDirectiveWiring = new ResourceDirectiveWiring();
  }

  @Nested
  class OnField {

    @Mock
    private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

    @Mock
    private GraphQLFieldDefinition element;

    @Mock
    private FieldDefinition fieldDefinition;

    @Mock
    private GraphQLFieldDefinition graphQlFieldDefinition;

    @Mock
    private GraphQLScalarType scalarType;

    @BeforeEach
    void setUp() {
      when(environment.getElement()).thenReturn(element);
      when(element.getDefinition()).thenReturn(fieldDefinition);
      when(fieldDefinition.getType()).thenReturn(mock(NonNullType.class));

      when(environment.getFieldDefinition()).thenReturn(graphQlFieldDefinition);
      when(graphQlFieldDefinition.getType()).thenReturn(scalarType);
    }

    @Test
    @DisplayName("Valid type declaration")
    void withValidScalar() {
      // Arrange
      when(scalarType.getName()).thenReturn("String");

      // Act & Assert
      assertDoesNotThrow(() -> resourceDirectiveWiring.onField(environment));
    }

    @Test
    @DisplayName("Invalid type declaration")
    void withInValidScalar() {
      // Used for exception message
      GraphQLFieldsContainer graphQlFieldsContainer = mock(GraphQLFieldsContainer.class);
      when(environment.getFieldsContainer()).thenReturn(graphQlFieldsContainer);
      when(graphQlFieldsContainer.getName()).thenReturn("Brewery");

      // Arrange
      when(scalarType.getName()).thenReturn("!String");

      // Act & Assert
      assertThrows(InvalidConfigurationException.class, () -> resourceDirectiveWiring.onField(environment));
    }
  }

  @Nested
  @DisplayName("Validation methods")
  class ValidationMethods {

    @Nested
    @DisplayName("Non-nullable field")
    class NonNullable {

      @Mock
      private GraphQLFieldDefinition element;

      @Mock
      private FieldDefinition fieldDefinition;

      @BeforeEach
      void setup() {
        when(element.getDefinition()).thenReturn(fieldDefinition);
      }

      @Test
      @DisplayName("Does not throw error")
      void nonNullableField() {
        // Arrange
        when(fieldDefinition.getType()).thenReturn(mock(NonNullType.class));

        // Act & Assert
        assertDoesNotThrow(() -> resourceDirectiveWiring.validateNonNullableField(element));
      }

      @Test
      @DisplayName("Throws error")
      void nullableListType() {
        // Arrange
        when(fieldDefinition.getType()).thenReturn(mock(ListType.class));

        // Act & Assert
        Error actual = assertThrows(ASSERTION_ERROR, () -> resourceDirectiveWiring.validateNonNullableField(element));

        assertThat(actual.getMessage(), containsString("can only be defined on non-nullable field"));
      }

    }

    @Nested
    @DisplayName("String Type Field")
    class StringType {

      @Mock
      private GraphQLScalarType scalarType;

      @Mock
      private GraphQLFieldDefinition fieldDefinition;

      @BeforeEach
      void setup() {
        when(fieldDefinition.getType()).thenReturn(scalarType);
      }

      @Test
      @DisplayName("Does not throw error")
      void whenString() {
        // Arrange
        when(scalarType.getName()).thenReturn("String");

        // Act & Assert
        assertDoesNotThrow(() -> resourceDirectiveWiring.validateOnStringField(fieldDefinition));
      }

      @Test
      @DisplayName("Throws error")
      void whenNotString() {
        // Arrange
        when(scalarType.getName()).thenReturn("List");

        // Act & Assert
        Error actual =
            assertThrows(ASSERTION_ERROR, () -> resourceDirectiveWiring.validateOnStringField(fieldDefinition));

        assertThat(actual.getMessage(), containsString("can only be defined on a String field"));
      }
    }
  }
}
