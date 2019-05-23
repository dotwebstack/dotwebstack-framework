package org.dotwebstack.framework.core.datafetchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.function.Supplier;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveDataFetcherTest {

  private static final String FIELD_NAME = "foo";

  private static final String FIELD_VALUE_1 = "bar";

  private static final String TRANSFORM_EXPR = "foo.length()";

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private IntegrationTestDataFetcher sourceDataFetcher;

  private TransformDirectiveDataFetcher transformDirectiveDataFetcher;

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  @BeforeEach
  void setUp() {
    transformDirectiveDataFetcher = new TransformDirectiveDataFetcher(jexlEngine);
  }

  @Test
  void onField_WrapsTransformDirectiveDataFetcher_ForScalarFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(FIELD_NAME)
        .type(Scalars.GraphQLString)
        .withDirective(GraphQLDirective.newDirective()
            .name(CoreDirectives.TRANSFORM_NAME)
            .argument(GraphQLArgument.newArgument()
                .name(CoreDirectives.TRANSFORM_ARG_EXPR)
                .type(Scalars.GraphQLString)
                .value(TRANSFORM_EXPR))
            .build())
        .build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn((Supplier) () -> sourceDataFetcher);
    when(sourceDataFetcher.get(dataFetchingEnvironment)).thenReturn(FIELD_VALUE_1);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    // Act
    Object result = transformDirectiveDataFetcher.get(dataFetchingEnvironment);

    // Assert
    assertThat(result, equalTo(3));
  }
}
