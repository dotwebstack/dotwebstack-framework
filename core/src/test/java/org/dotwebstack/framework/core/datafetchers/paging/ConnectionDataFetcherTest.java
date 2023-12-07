package org.dotwebstack.framework.core.datafetchers.paging;

import static graphql.schema.GraphQLArgument.newArgument;
import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Map;
import org.dotwebstack.framework.core.RequestValidationException;
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

  @Mock
  private PagingConfiguration pagingConfiguration;

  private ConnectionDataFetcher connectionDataFetcher;

  @BeforeEach
  void beforeEach() {
    connectionDataFetcher = new ConnectionDataFetcher(pagingConfiguration);
    GraphQLFieldDefinition fieldDefinition = mock(GraphQLFieldDefinition.class);

    when(fieldDefinition.getArgument(FIRST_ARGUMENT_NAME)).thenReturn(newArgument().name(FIRST_ARGUMENT_NAME)
        .type(Scalars.GraphQLInt)
        .defaultValueProgrammatic(10)
        .build());

    when(fieldDefinition.getArgument(OFFSET_ARGUMENT_NAME)).thenReturn(newArgument().name(OFFSET_ARGUMENT_NAME)
        .type(Scalars.GraphQLInt)
        .defaultValueProgrammatic(0)
        .build());

    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    lenient().when(pagingConfiguration.getFirstMaxValue())
        .thenReturn(100);
    lenient().when(pagingConfiguration.getOffsetMaxValue())
        .thenReturn(10000);
  }

  @Test
  void get_returnsResult_forPagingArguments() {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of(FIRST_ARGUMENT_NAME, 2, OFFSET_ARGUMENT_NAME, 20));

    Object result = connectionDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, CoreMatchers.instanceOf(DataFetcherResult.class));

    var dataFetcherResult = (DataFetcherResult<?>) result;

    assertThat(dataFetcherResult.getData(), equalTo(Map.of(OFFSET_ARGUMENT_NAME, 20,
        PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), 20, PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), 2)));
  }

  @Test
  void get_returnsResult_forDefault() {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of());

    Object result = connectionDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, CoreMatchers.instanceOf(DataFetcherResult.class));

    var dataFetcherResult = (DataFetcherResult<?>) result;

    assertThat(dataFetcherResult.getData(), equalTo(Map.of(OFFSET_ARGUMENT_NAME, 0,
        PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), 0, PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), 10)));
  }

  @Test
  void get_returnsResult_forNegativeMaxValue() {
    lenient().when(pagingConfiguration.getFirstMaxValue())
        .thenReturn(-1);
    lenient().when(pagingConfiguration.getOffsetMaxValue())
        .thenReturn(-1);

    when(dataFetchingEnvironment.getArguments())
        .thenReturn(Map.of(FIRST_ARGUMENT_NAME, 0, OFFSET_ARGUMENT_NAME, 10000000));

    Object result = connectionDataFetcher.get(dataFetchingEnvironment);

    assertThat(result, CoreMatchers.instanceOf(DataFetcherResult.class));

    var dataFetcherResult = (DataFetcherResult<?>) result;

    assertThat(dataFetcherResult.getData(), equalTo(Map.of(OFFSET_ARGUMENT_NAME, 10000000,
        PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), 10000000, PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), 0)));
  }

  @Test
  void get_throwsException_forInvalidFirstArgumentValue() {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of(FIRST_ARGUMENT_NAME, 101, OFFSET_ARGUMENT_NAME, 20));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'first' is not allowed to be higher than 100."));
  }

  @Test
  void get_throwsException_forInvalidOffsetArgumentValue() {
    when(dataFetchingEnvironment.getArguments())
        .thenReturn(Map.of(FIRST_ARGUMENT_NAME, 2, OFFSET_ARGUMENT_NAME, 10001));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'offset' is not allowed to be higher than 10000."));
  }
}
