package org.dotwebstack.framework.backend.rdf4j.graphql;

import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_EXAMPLE;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_PATH;
import static org.dotwebstack.framework.test.Constants.BUILDING_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_PATH;
import static org.dotwebstack.framework.test.Constants.BUILDING_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
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
        .name(BUILDING_IDENTIFIER_FIELD)
        .path(BUILDING_IDENTIFIER_PATH)
        .build());
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_IDENTIFIER_PATH, BUILDING_IDENTIFIER_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BUILDING_IDENTIFIER_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsLiteral_ForForeignScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BUILDING_BUILT_AT_FIELD)
        .path(BUILDING_BUILT_AT_PATH)
        .build());
    Model model = new ModelBuilder()
        .add(BUILDING_EXAMPLE_1, BUILDING_BUILT_AT_PATH, BUILDING_BUILT_AT_EXAMPLE)
        .build();
    when(environment.getFieldType()).thenReturn(
        org.dotwebstack.framework.core.graphql.scalars.Scalars.DATETIME);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BUILDING_BUILT_AT_EXAMPLE)));
  }

  @Test
  void get_ReturnsNull_ForAbsentScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BUILDING_IDENTIFIER_FIELD)
        .path(BUILDING_IDENTIFIER_PATH)
        .build());
    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BUILDING_EXAMPLE_1));

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
        .name(BUILDING_TYPE)
        .build());
    when(environment.getSource()).thenReturn(new QuerySolution(model, BUILDING_EXAMPLE_1));

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        valueFetcher.get(environment));
  }

}
