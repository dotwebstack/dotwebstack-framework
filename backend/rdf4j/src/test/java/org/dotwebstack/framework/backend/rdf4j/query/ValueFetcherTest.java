package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValueFetcherTest {

  @Mock
  private DataFetchingEnvironment environment;

  @Test
  void get_ReturnsConvertedLiteral_ForBuiltInScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(BREWERY_IDENTIFIER_PATH)
        .build());
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_IDENTIFIER_PATH,
            BREWERY_IDENTIFIER_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_IDENTIFIER_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsString_ForForeignScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_FOUNDED_FIELD)
        .path(BREWERY_FOUNDED_PATH)
        .build());
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_FOUNDED_PATH, BREWERY_FOUNDED_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(
        CoreScalars.DATETIME);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_FOUNDED_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsNull_ForAbsentScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(BREWERY_IDENTIFIER_PATH)
        .build());
    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  void get_ThrowsException_ForNonScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder().build());
    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(GraphQLObjectType.newObject()
        .name(BREWERY_TYPE)
        .build());
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        valueFetcher.get(environment));
  }

}
