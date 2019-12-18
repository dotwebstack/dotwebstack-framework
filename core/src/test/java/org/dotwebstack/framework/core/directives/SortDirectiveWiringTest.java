package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SortDirectiveWiringTest {

  private static final Class<AssertionError> ASSERTION_ERROR = AssertionError.class;

  private SortDirectiveWiring sortDirectiveWiring;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLArgument> environment;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private GraphQLArgument argument;

  @BeforeEach
  void doBefore() {
    sortDirectiveWiring = new SortDirectiveWiring(null);
  }

  @Nested
  class OnArgument {

    @Test
    void withValidScalar() {
      // Arrange
      when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
      when(environment.getElement()).thenReturn(argument);
      when(fieldDefinition.getType()).thenReturn(GraphQLList.list(GraphQLString));
      when(environment.getElement()).thenReturn(argument);
      when(argument.getDefaultValue()).thenReturn(List.of(Map.of("order", "ASC")));
      when(argument.getType()).thenReturn(GraphQLList.list(GraphQLInputObjectType.newInputObject()
          .name("SortField")
          .build()));

      // Act & Assert
      assertDoesNotThrow(() -> sortDirectiveWiring.onArgument(environment));
    }
  }

  @Nested
  @DisplayName("Validate methods")
  class ValidateMethods {

    @Nested
    @DisplayName("List Size")
    class ListSize {

      @Test
      @DisplayName("Does not throw error")
      void withListSizeExactlyOne() {
        // Act & Assert
        assertDoesNotThrow(() -> sortDirectiveWiring.validateListSize(List.of("1")));
      }

      @Test
      @DisplayName("Throws error")
      void withListSizeGreaterThenOne() {
        // Act & Assert
        assertThrows(ASSERTION_ERROR, () -> sortDirectiveWiring.validateListSize(List.of("1", "2")));
      }

    }

    @Nested
    @DisplayName("List Type")
    class ListType {

      @Test
      @DisplayName("Does not throw error")
      void whenList() {
        // Act & Assert
        assertDoesNotThrow(() -> sortDirectiveWiring.validateListType(GraphQLList.list(Scalars.GraphQLString)));
      }

      @Test
      @DisplayName("Throws error")
      void whenNotList() {
        // Act & Assert
        assertThrows(ASSERTION_ERROR, () -> sortDirectiveWiring.validateListType(Scalars.GraphQLString));
      }
    }

    @Nested
    @DisplayName("SortField field")
    class SortFieldField {

      @Test
      @DisplayName("Does not throw error")
      void whenPresent() {
        // Act & Assert
        assertDoesNotThrow(() -> sortDirectiveWiring.validateFieldArgumentDoesNotExist(Map.of("test", "beer")));
      }

      @Test
      @DisplayName("Throws error")
      void whenMissing() {
        // Act & Assert
        assertThrows(ASSERTION_ERROR,
            () -> sortDirectiveWiring.validateFieldArgumentDoesNotExist(Map.of("field", "beer")));

      }

    }

    @Nested
    @DisplayName("SortField list")
    class SortFieldList {

      @Test
      @DisplayName("Does not throw error")
      void whenSortFieldList() {
        // Act & Assert
        assertDoesNotThrow(() -> sortDirectiveWiring.validateSortFieldList(GraphQLList.list(Scalars.GraphQLString),
            "SortField", "sort"));
      }

      @Test
      @DisplayName("Throws error")
      void whenNotList() {
        // Act & Assert
        assertThrows(ASSERTION_ERROR,
            () -> sortDirectiveWiring.validateSortFieldList(Scalars.GraphQLString, "SortField", "sort"));
      }

      @Test
      @DisplayName("Throws error")
      void whenNotSortField() {
        // Act & Assert
        assertThrows(ASSERTION_ERROR,
            () -> sortDirectiveWiring.validateSortFieldList(Scalars.GraphQLString, "Beer", "sort"));
      }
    }
  }
}
