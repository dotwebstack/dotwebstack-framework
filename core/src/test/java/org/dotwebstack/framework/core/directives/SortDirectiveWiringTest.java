package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SortDirectiveWiringTest {

  private SortDirectiveWiring sortDirectiveWiring;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLArgument> environment;

  @Mock
  private GraphQLFieldsContainer fieldsContainer;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private GraphQLArgument argument;

  @BeforeEach
  void doBefore() {
    sortDirectiveWiring = new SortDirectiveWiring(null);
  }

  @Test
  void validate_onArgument_withValidScalar() {
    // Arrange
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn("Beer");
    when(fieldDefinition.getType()).thenReturn(GraphQLList.list(GraphQLString));
    when(environment.getFieldsContainer()).thenReturn(fieldsContainer);
    when(fieldsContainer.getName()).thenReturn("brewery");
    when(environment.getElement()).thenReturn(argument);
    when(argument.getName()).thenReturn("sort");
    when(argument.getDefaultValue()).thenReturn(List.of(Map.of("order", "ASC")));
    when(argument.getType()).thenReturn(GraphQLList.list(GraphQLInputObjectType.newInputObject()
        .name("SortField")
        .build()));

    // Act & Assert
    assertDoesNotThrow(() -> sortDirectiveWiring.onArgument(environment));
  }

  @Test
  void validateListSize_doesNotThrowError_WithListSizeExactlyOne() {
    // Act & Assert
    assertDoesNotThrow(() -> sortDirectiveWiring.validateListSize(List.of("1"), "Beer", "brewery"));
  }

  @Test
  void validateListSize_throwsError_withListSizeGreaterThenOne() {
    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> sortDirectiveWiring.validateListSize(List.of("1", "2"), "Beer", "brewery"));
  }

  @Test
  void validateSortFieldField_doesNotThrowError_whenPresent() {
    // Act & Assert
    assertDoesNotThrow(
        () -> sortDirectiveWiring.validateFieldArgumentDoesNotExist(Map.of("test", "beer"), "Beer", "brewery"));
  }

  @Test
  void validateSortFieldField_ThrowsError_whenMissing() {
    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> sortDirectiveWiring.validateFieldArgumentDoesNotExist(Map.of("field", "beer"), "Beer", "brewery"));

  }

  @Test
  void validateListType_throwsError_whenNotList() {
    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> sortDirectiveWiring.validateListType(Scalars.GraphQLString, "Beer", "brewery"));
  }

  @Test
  void validateListType_doesNotThrowError_whenList() {
    // Act & Assert
    assertDoesNotThrow(
        () -> sortDirectiveWiring.validateListType(GraphQLList.list(Scalars.GraphQLString), "Beer", "brewery"));
  }

  @Test
  void validateSortFieldList_throwsError_whenNotList() {
    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> sortDirectiveWiring.validateSortFieldList(Scalars.GraphQLString, "SortField", "Beer", "brewery", "sort"));
  }

  @Test
  void validateSortFieldList_throwsError_whenNotSortField() {
    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> sortDirectiveWiring.validateSortFieldList(Scalars.GraphQLString, "Beer", "Beer", "brewery", "sort"));
  }

  @Test
  void validateSortFieldList_doesNothrowError_whenSortFieldList() {
    // Act & Assert
    assertDoesNotThrow(() -> sortDirectiveWiring.validateSortFieldList(GraphQLList.list(Scalars.GraphQLString),
        "SortField", "Beer", "brewery", "sort"));
  }
}
