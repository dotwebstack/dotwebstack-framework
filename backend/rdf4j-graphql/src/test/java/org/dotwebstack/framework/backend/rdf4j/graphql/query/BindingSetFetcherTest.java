package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import org.dotwebstack.framework.backend.rdf4j.graphql.Constants;
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
        .name(Constants.BUILDING_IDENTIFIER_NAME)
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
        .name(Constants.BUILDING_IDENTIFIER_NAME)
        .type(Scalars.GraphQLID)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet
        .addBinding(Constants.BUILDING_IDENTIFIER_NAME, Constants.BUILDING_IDENTIFIER_EXAMPLE);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    MatcherAssert
        .assertThat(result, CoreMatchers.is(Constants.BUILDING_IDENTIFIER_EXAMPLE.stringValue()));
  }

  @Test
  void get_returnsLiteral_forCustomScalarProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(Constants.BUILDING_BUILT_AT_NAME)
        .type(org.dotwebstack.framework.core.scalars.Scalars.DATETIME)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet.addBinding(Constants.BUILDING_BUILT_AT_NAME, Constants.BUILDING_BUILT_AT_EXAMPLE);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    MatcherAssert.assertThat(result, CoreMatchers.is(Constants.BUILDING_BUILT_AT_EXAMPLE));
  }

  @Test
  void get_returnsString_forIriProperty() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(Constants.BUILDING_IDENTIFIER_NAME)
        .type(Scalars.GraphQLID)
        .build();
    MapBindingSet bindingSet = new MapBindingSet();
    bindingSet.addBinding(Constants.BUILDING_IDENTIFIER_NAME, RDF.SUBJECT);
    DataFetchingEnvironment environment = createEnvironment(fieldDefinition, bindingSet);

    // Act
    Object result = bindingSetFetcher.get(environment);

    // Assert
    assertThat(result, is(RDF.SUBJECT.stringValue()));
  }

  private static DataFetchingEnvironment createEnvironment(GraphQLFieldDefinition fieldDefinition,
      BindingSet bindingSet) {
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getSource()).thenReturn(bindingSet);
    when(environment.getFieldDefinition()).thenReturn(fieldDefinition);

    return environment;
  }

}
