package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_EXAMPLE;
import static org.dotwebstack.framework.test.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.test.Constants.BUILDING_IDENTIFIER_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class BindingSetFetcherTest {

  private final BindingSetFetcher bindingSetFetcher = new BindingSetFetcher();

  @Test
  void get_returnsNull_forAbsentProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_NAME)
        .type(Scalars.GraphQLID)
        .build();
    BindingSet bindingSet = new EmptyBindingSet();
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  void get_returnsSpecificType_forBuiltInScalarProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_NAME)
        .type(Scalars.GraphQLID)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet
        .addBinding(BUILDING_IDENTIFIER_NAME, BUILDING_IDENTIFIER_EXAMPLE_1);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    MatcherAssert
        .assertThat(result,
            CoreMatchers.is(BUILDING_IDENTIFIER_EXAMPLE_1.stringValue()));
  }

  @Test
  void get_returnsLiteral_forCustomScalarProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_BUILT_AT_FIELD)
        .type(org.dotwebstack.framework.graphql.scalars.Scalars.DATETIME)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet.addBinding(BUILDING_BUILT_AT_FIELD, BUILDING_BUILT_AT_EXAMPLE);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    MatcherAssert.assertThat(result, CoreMatchers.is(BUILDING_BUILT_AT_EXAMPLE));
  }

  @Test
  void get_returnsString_forIriProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(BUILDING_IDENTIFIER_NAME)
        .type(Scalars.GraphQLID)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet.addBinding(BUILDING_IDENTIFIER_NAME, RDF.SUBJECT);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(RDF.SUBJECT)));
  }

  private static DataFetchingEnvironment createEnvironment(GraphQLFieldDefinition fieldDefinition,
      BindingSet bindingSet) {
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getSource()).thenReturn(bindingSet);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    return environment;
  }

}
