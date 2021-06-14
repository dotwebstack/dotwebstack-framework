package org.dotwebstack.framework.core.datafetchers.paging;

import static graphql.schema.GraphQLArgument.newArgument;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionDataFetcherTest {

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  private final ConnectionDataFetcher connectionDataFetcher = new ConnectionDataFetcher();

  @BeforeEach
  void beforeEach() {
    GraphQLFieldDefinition fieldDefinition = mock(GraphQLFieldDefinition.class);

    when(fieldDefinition.getArgument(FIRST_ARGUMENT_NAME)).thenReturn(newArgument().name(FIRST_ARGUMENT_NAME)
        .type(Scalars.GraphQLInt)
        .defaultValue(1)
        .build());

    when(fieldDefinition.getArgument(OFFSET_ARGUMENT_NAME)).thenReturn(newArgument().name(OFFSET_ARGUMENT_NAME)
        .type(Scalars.GraphQLInt)
        .defaultValue(10)
        .build());

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
  }

  @Test
  void get_returnsResult_forPagingArguments() throws Exception {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of(FIRST_ARGUMENT_NAME, 2, OFFSET_ARGUMENT_NAME, 20));

    Object result = connectionDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, CoreMatchers.instanceOf(DataFetcherResult.class));

    var dataFetcherResult = (DataFetcherResult<?>) result;

    assertThat(dataFetcherResult.getData(), equalTo(Map.of(OFFSET_ARGUMENT_NAME, 20)));
    assertThat(dataFetcherResult.getLocalContext(), equalTo(PagingDataFetcherContext.builder()
        .first(2)
        .offset(20)
        .build()));
  }

  @Test
  void get_returnsResult_forDefault() throws Exception {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of());

    Object result = connectionDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, CoreMatchers.instanceOf(DataFetcherResult.class));

    var dataFetcherResult = (DataFetcherResult<?>) result;

    assertThat(dataFetcherResult.getData(), equalTo(Map.of(OFFSET_ARGUMENT_NAME, 10)));
    assertThat(dataFetcherResult.getLocalContext(), equalTo(PagingDataFetcherContext.builder()
        .first(1)
        .offset(10)
        .build()));
  }
}
