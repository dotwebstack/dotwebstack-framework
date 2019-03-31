package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.ModelFetcher;
import org.dotwebstack.framework.test.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparqlDirectiveWiringTest {

  @Mock
  private ModelFetcher modelFetcher;

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

  @Mock
  private GraphQLCodeRegistry.Builder codeRegistry;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private GraphQLFieldsContainer parentType;

  private SparqlDirectiveWiring sparqlDirectiveWiring;

  @BeforeEach
  void setUp() {
    sparqlDirectiveWiring = new SparqlDirectiveWiring(modelFetcher);
    when(environment.getElement()).thenReturn(fieldDefinition);
  }

  @Test
  void onField_RegistersDataFetcher_ForObjectOutputType() {
    // Arrange
    GraphQLObjectType outputType = GraphQLObjectType.newObject()
        .name(Constants.BUILDING_TYPE)
        .build();
    when(fieldDefinition.getType()).thenReturn(outputType);
    when(environment.getFieldsContainer()).thenReturn(parentType);
    when(environment.getCodeRegistry()).thenReturn(codeRegistry);

    // Act
    GraphQLFieldDefinition result = sparqlDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(equalTo(fieldDefinition)));
    verify(codeRegistry).dataFetcher(eq(parentType), eq(fieldDefinition), eq(modelFetcher));
  }

  @Test
  void onField_ThrowsException_ForScalarOutputType() {
    // Arrange
    when(fieldDefinition.getType()).thenReturn(Scalars.GraphQLString);

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        sparqlDirectiveWiring.onField(environment));
  }

  @Test
  void onField_ThrowsException_ForEnumOutputType() {
    // Arrange
    when(fieldDefinition.getType()).thenReturn(GraphQLEnumType.newEnum().name("foo").build());

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        sparqlDirectiveWiring.onField(environment));
  }

}
