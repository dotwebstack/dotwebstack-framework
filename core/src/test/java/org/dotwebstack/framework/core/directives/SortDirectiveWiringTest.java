package org.dotwebstack.framework.core.directives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SortDirectiveWiringTest {

  private SortDirectiveWiring sortDirectiveWiring;

  @BeforeEach
  void doBefore() {
    sortDirectiveWiring = new SortDirectiveWiring(null);
  }

  @Test
  void validateListSize_doesNotThrowError_WithListSizeExactlyOne() {
    // Act & Assert
    assertDoesNotThrow(() -> sortDirectiveWiring.validateListSize(List.of("1"), "Beer", "brewery"));
  }

  @Test
  void validateListSize_throwsError_WithListSizeGreaterThenOne() {
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
