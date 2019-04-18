package org.dotwebstack.framework.core.graphql.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveWiringTest {

  private static final String FIELD_NAME = "foo";

  private static final String FIELD_VALUE_1 = "bar";

  private static final String FIELD_VALUE_2 = "bazzz";

  private static final String TRANSFORM_EXPR = "foo.length()";

  @Mock
  private SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment;

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private DataFetcher<Object> dataFetcher;

  @Mock
  private GraphQLFieldsContainer parentType;

  @Mock
  private GraphQLCodeRegistry.Builder codeRegistry;

  @Captor
  private ArgumentCaptor<DataFetcher<Object>> dataFetcherCaptor;

  private final JexlEngine jexlEngine = new JexlBuilder()
      .silent(false)
      .strict(true)
      .create();

  private final TransformDirectiveWiring transformDirectiveWiring = new TransformDirectiveWiring(
      jexlEngine);

  @BeforeEach
  void setUp() {
    when(environment.getFieldsContainer()).thenReturn(parentType);
  }

  @Test
  void onField_WrapsExistingFetcher_ForScalarFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(Scalars.GraphQLString)
        .build();
    prepareEnvironment(fieldDefinition, FIELD_VALUE_1);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment),
        is(equalTo(FIELD_VALUE_1.length())));
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void onField_WrapsExistingFetcher_ForScalarFieldWithoutValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(Scalars.GraphQLString)
        .build();
    prepareEnvironment(fieldDefinition, null);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment), is(nullValue()));
  }

  @Test
  void onField_WrapsExistingFetcher_ForNonNullScalarFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
        .build();
    prepareEnvironment(fieldDefinition, FIELD_VALUE_1);

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment),
        is(equalTo(FIELD_VALUE_1.length())));
  }

  @Test
  void onField_WrapsExistingFetcher_ForListScalarFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLList.list(Scalars.GraphQLString))
        .build();
    prepareEnvironment(fieldDefinition, ImmutableList.of(FIELD_VALUE_1, FIELD_VALUE_2));

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment),
        is(equalTo(ImmutableList.of(FIELD_VALUE_1.length(), FIELD_VALUE_2.length()))));
  }

  @Test
  void onField_WrapsExistingFetcher_ForNonNullListScalarFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLNonNull.nonNull(GraphQLList.list(
            GraphQLNonNull.nonNull(Scalars.GraphQLString))))
        .build();
    prepareEnvironment(fieldDefinition, ImmutableList.of(FIELD_VALUE_1, FIELD_VALUE_2));

    // Act
    GraphQLFieldDefinition result = transformDirectiveWiring.onField(environment);

    // Assert
    assertThat(result, is(sameInstance(fieldDefinition)));
    verify(codeRegistry)
        .dataFetcher(eq(parentType), eq(fieldDefinition), dataFetcherCaptor.capture());
    assertThat(dataFetcherCaptor.getValue().get(dataFetchingEnvironment),
        is(equalTo(ImmutableList.of(FIELD_VALUE_1.length(), FIELD_VALUE_2.length()))));
  }

  @Test
  void onField_ThrowsException_ForNonScalarField() {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(GraphQLObjectType.newObject().name(FIELD_NAME))
        .build();
    when(environment.getElement()).thenReturn(fieldDefinition);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        transformDirectiveWiring.onField(environment));
  }

  private void prepareEnvironment(GraphQLFieldDefinition fieldDefinition, Object value)
      throws Exception {
    when(environment.getElement()).thenReturn(fieldDefinition);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(dataFetcher.get(dataFetchingEnvironment)).thenReturn(value);
    when(codeRegistry.getDataFetcher(parentType, fieldDefinition)).thenReturn(dataFetcher);
    when(environment.getCodeRegistry()).thenReturn(codeRegistry);
    when(environment.getDirective()).thenReturn(GraphQLDirective.newDirective()
        .name(CoreDirectives.TRANSFORM_NAME)
        .argument(GraphQLArgument.newArgument()
            .name(CoreDirectives.TRANSFORM_ARG_EXPR)
            .type(Scalars.GraphQLString)
            .value(TRANSFORM_EXPR))
        .build());
  }

}
