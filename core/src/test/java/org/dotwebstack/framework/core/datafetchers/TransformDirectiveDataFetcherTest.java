package org.dotwebstack.framework.core.datafetchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.scalars.CoreCoercing;
import org.dotwebstack.framework.core.scalars.DateCoercing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformDirectiveDataFetcherTest {

  private static final String LENGTH_NAME = "foo";

  private static final String LENGTH_VALUE = "bar";

  private static final String LENGTH_EXPR = "foo.length()";

  private static final String LENGTH_TYPE = "String";

  private static final String DATE_NAME = "date";

  private static final String DATE_VALUE = "1993-08-05";

  private static final String DATE_EXPR = "date.getYear()";

  private static final String DATE_TYPE = "LocalDate";

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private IntegrationTestDataFetcher sourceDataFetcher;

  @Mock
  private TransformDirectiveDataFetcher transformDirectiveDataFetcher;

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final List<CoreCoercing<?>> coercings = Arrays.asList(new CoreCoercing<?>[] {new DateCoercing()});

  @BeforeEach
  void setUp() {
    transformDirectiveDataFetcher = new TransformDirectiveDataFetcher(jexlEngine, coercings);
  }

  @Test
  void onField_WrapsTransformDirectiveDataFetcher_ForIntegerFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(LENGTH_NAME)
        .type(Scalars.GraphQLString)
        .withDirective(GraphQLDirective.newDirective()
            .name(CoreDirectives.TRANSFORM_NAME)
            .argument(GraphQLArgument.newArgument()
                .name(CoreDirectives.TRANSFORM_ARG_EXPR)
                .type(Scalars.GraphQLString)
                .value(LENGTH_EXPR))
            .argument(GraphQLArgument.newArgument()
                .name(CoreDirectives.TRANSFORM_ARG_TYPE)
                .type(Scalars.GraphQLString)
                .value(LENGTH_TYPE))
            .build())
        .build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn((Supplier) () -> sourceDataFetcher);
    when(sourceDataFetcher.get(dataFetchingEnvironment)).thenReturn(LENGTH_VALUE);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    // Act
    Object result = transformDirectiveDataFetcher.get(dataFetchingEnvironment);

    // Assert
    assertThat(result, equalTo(3));
  }

  @Test
  void onField_WrapsTransformDirectiveDataFetcher_ForDateFieldWithValue() throws Exception {
    // Arrange
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name(DATE_NAME)
        .type(Scalars.GraphQLString)
        .withDirective(GraphQLDirective.newDirective()
            .name(CoreDirectives.TRANSFORM_NAME)
            .argument(GraphQLArgument.newArgument()
                .name(CoreDirectives.TRANSFORM_ARG_EXPR)
                .type(Scalars.GraphQLString)
                .value(DATE_EXPR))
            .argument(GraphQLArgument.newArgument()
                .name(CoreDirectives.TRANSFORM_ARG_TYPE)
                .type(Scalars.GraphQLString)
                .value(DATE_TYPE))
            .build())
        .build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn((Supplier) () -> sourceDataFetcher);
    when(sourceDataFetcher.get(dataFetchingEnvironment)).thenReturn(DATE_VALUE);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    // Act
    Object result = transformDirectiveDataFetcher.get(dataFetchingEnvironment);

    // Assert
    assertThat(result, equalTo(1993));
  }
}
