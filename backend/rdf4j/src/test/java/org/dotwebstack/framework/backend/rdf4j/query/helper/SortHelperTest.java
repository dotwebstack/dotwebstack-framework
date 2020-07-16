package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import java.util.stream.Stream;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfValue;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SortHelperTest {

  @Mock
  private GraphQLFieldDefinition fieldDefinition1;

  @ParameterizedTest
  @MethodSource
  public void getDefaultOrderByValue_returnsEmptyString_for_nonNumeric(GraphQLScalarType type) {
    // Arrange
    when(fieldDefinition1.getType()).thenReturn(type);

    // Act
    RdfValue value = SortHelper.getDefaultOrderByValue(fieldDefinition1);

    // Assert
    assertThat(value.getQueryString(), is("\"\""));
  }

  private static Stream<GraphQLScalarType> getDefaultOrderByValue_returnsEmptyString_for_nonNumeric() {
    return Stream.of(Scalars.GraphQLString, Scalars.GraphQLBoolean, Scalars.GraphQLChar, Scalars.GraphQLID);
  }

  @ParameterizedTest
  @MethodSource
  public void getDefaultOrderByValue_returnsZero_for_numeric(GraphQLScalarType type) {
    // Arrange
    when(fieldDefinition1.getType()).thenReturn(type);

    // Act
    RdfValue value = SortHelper.getDefaultOrderByValue(fieldDefinition1);

    // Assert
    assertThat(value.getQueryString(), is("0"));
  }

  private static Stream<GraphQLScalarType> getDefaultOrderByValue_returnsZero_for_numeric() {
    return SortHelper.NUMERIC_TYPES.stream();
  }
}
