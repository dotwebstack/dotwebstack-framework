package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.DESCRIBE_QUERY_COMMAND;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.SELECT_QUERY_COMMAND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GraphQlFieldDefinitionHelperTest {

  private static final String SELECT_QUERY = "SELECT DISTINCT ?breweries_with_query_ref_as_iri WHERE {\n"
      + "   ?subject <https://github.com/dotwebstack/beer/def#brewery> ?breweries_with_query_ref_as_iri\n" + "}";

  private static final String DESCRIBE_QUERY = "DESCRIBE ?breweries WHERE {\n"
      + "   ?beerIRI <https://github.com/dotwebstack/beer/def#brewery> ?breweries\n" + "}";

  @ParameterizedTest(name = "Check equals {2}")
  @CsvSource({"name, name, true", "name, notname, false"})
  void graphQlFieldDefinitionIsOfTypeTest(String outputTypeName, String scalarTypeName, boolean success) {
    // Arrange
    GraphQLOutputType graphQlOutputTypeMock = mock(GraphQLOutputType.class);
    GraphQLScalarType graphQlScalarTypeMock = mock(GraphQLScalarType.class);
    when(graphQlOutputTypeMock.getName()).thenReturn(outputTypeName);
    when(graphQlScalarTypeMock.getName()).thenReturn(scalarTypeName);

    // Act
    boolean result =
        GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType(graphQlOutputTypeMock, graphQlScalarTypeMock);

    // Assert
    assertEquals(result, success);
  }

  @Test
  void graphQlFieldDefinitionIsOfTypeWithNullTest() {
    // Arrange
    GraphQLOutputType graphQlOutputTypeMock = mock(GraphQLOutputType.class);
    GraphQLScalarType graphQlScalarTypeMock = mock(GraphQLScalarType.class);

    // Act / Assert
    assertThrows(NullPointerException.class,
        () -> GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType(graphQlOutputTypeMock, null));

    // Act / Assert
    assertThrows(NullPointerException.class,
        () -> GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType(null, graphQlScalarTypeMock));
  }

  @Test
  void successfulValidationForSelectTest() {
    // Arrange / Act / Assert
    assertDoesNotThrow(() -> GraphQlFieldDefinitionHelper.validateQueryHasCommand(SELECT_QUERY, SELECT_QUERY_COMMAND));
  }

  @Test
  void successfulValidationForDescribeTest() {
    // Arrange / Act / Assert
    assertDoesNotThrow(
        () -> GraphQlFieldDefinitionHelper.validateQueryHasCommand(DESCRIBE_QUERY, DESCRIBE_QUERY_COMMAND));
  }

  @Test
  void validationTestThrowsInValidConfigurationException() {
    // Arrange / Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> GraphQlFieldDefinitionHelper.validateQueryHasCommand(SELECT_QUERY, DESCRIBE_QUERY_COMMAND));
  }


}
