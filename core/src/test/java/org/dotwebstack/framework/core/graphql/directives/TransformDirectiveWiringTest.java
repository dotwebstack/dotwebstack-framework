package org.dotwebstack.framework.core.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveWiringTest {

  private static final String FIELD_NAME = "foo";

  private static final String FIELD_VALUE = "bar";

  private static final String TRANSFORM_EXPR = "foo.length()";

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private DataFetcher dataFetcher;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private GraphQLFieldsContainer parentType;

  @Mock
  private GraphQLCodeRegistry.Builder codeRegistry;

  @Captor
  private ArgumentCaptor<DataFetcher> dataFetcherCaptor;

  private final JexlEngine jexlEngine = new JexlBuilder()
      .silent(false)
      .strict(true)
      .create();

  private final TransformDirectiveWiring transformDirectiveWiring = new TransformDirectiveWiring(
      jexlEngine);

  @BeforeEach
  void setUp() {
    when(environment.getFieldsContainer()).thenReturn(parentType);
    when(environment.getElement()).thenReturn(fieldDefinition);
  }

  @Test
  void onField_WrapsExistingFetcher_ForScalarField() throws Exception {
    // Arrange
    when(fieldDefinition.getName()).thenReturn(FIELD_NAME);
    when(fieldDefinition.getType()).thenReturn(Scalars.GraphQLString);

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(FIELD_VALUE);

    when(codeRegistry.getDataFetcher(parentType, fieldDefinition)).thenReturn(dataFetcher);
    when(environment.getCodeRegistry()).thenReturn(codeRegistry);
    when(environment.getDirective()).thenReturn(GraphQLDirective.newDirective()
        .name(CoreDirectives.TRANSFORM_NAME)
        .argument(GraphQLArgument.newArgument()
            .name(CoreDirectives.TRANSFORM_ARG_EXPR)
            .type(Scalars.GraphQLString)
            .value(TRANSFORM_EXPR))
        .build());

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment),
        equalTo(FIELD_VALUE.length()));
  }

  @Test
  void onField_ThrowsException_ForNonScalarField() {
    // Arrange
    when(fieldDefinition.getType()).thenReturn(GraphQLObjectType.newObject()
        .name(FIELD_NAME)
        .build());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        transformDirectiveWiring.onField(environment));
  }

}
