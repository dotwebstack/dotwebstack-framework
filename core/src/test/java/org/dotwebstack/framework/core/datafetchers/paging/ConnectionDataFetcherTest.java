package org.dotwebstack.framework.core.datafetchers.paging;

import static graphql.schema.GraphQLArgument.newArgument;
import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.DataFetcherResult;
import graphql.language.FieldDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.FieldWiringEnvironment;
import java.util.Map;
import org.dotwebstack.framework.core.RequestValidationException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionDataFetcherTest {

  private static final String SKIP_BEFORE_TAG = "skip_before_each";

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private PagingConfiguration pagingConfiguration;

  @Mock
  private FieldWiringEnvironment environment;

  private ConnectionDataFetcher connectionDataFetcher;

  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    if (testInfo.getTags()
        .contains(SKIP_BEFORE_TAG)) {
      return;
    }
    connectionDataFetcher = new ConnectionDataFetcher(pagingConfiguration, environment);
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
  @Tag(SKIP_BEFORE_TAG)
  @ExtendWith(OutputCaptureExtension.class)
  void initialize_getWarnings_forPagingMaxValues(CapturedOutput output) {
    var fieldFieldDefinition = mock(FieldDefinition.class);

    when(pagingConfiguration.getFirstMaxValue()).thenReturn(-1);
    when(pagingConfiguration.getOffsetMaxValue()).thenReturn(-1);
    when(environment.getFieldDefinition()).thenReturn(fieldFieldDefinition);
    when(fieldFieldDefinition.getName()).thenReturn("ObjName");

    connectionDataFetcher = new ConnectionDataFetcher(pagingConfiguration, environment);

    assertThat(output.getAll(), stringContainsInOrder("One or both paging arguments max values are negative,"
        + " this may result in a slow responses for type ObjName. 'firstMax': -1, 'offsetMax':-1"));
  }

  @Test
  @Tag(SKIP_BEFORE_TAG)
  @ExtendWith(OutputCaptureExtension.class)
  void initialize_getWarnings_forPagingMaxValuesWithUnknown(CapturedOutput output) {
    when(pagingConfiguration.getFirstMaxValue()).thenReturn(-1);
    when(pagingConfiguration.getOffsetMaxValue()).thenReturn(-1);

    connectionDataFetcher = new ConnectionDataFetcher(pagingConfiguration, environment);

    assertThat(output.getAll(), stringContainsInOrder("One or both paging arguments max values are negative,"
        + " this may result in a slow responses for type unknown. 'firstMax': -1, 'offsetMax':-1"));
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
  void get_throwsException_forInvalidTooHighFirstArgumentValue() {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of(FIRST_ARGUMENT_NAME, 101, OFFSET_ARGUMENT_NAME, 20));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'first' is not allowed to be higher than 100."));
  }

  @Test
  void get_throwsException_forInvalidNegativeFirstArgumentValue() {
    when(dataFetchingEnvironment.getArguments()).thenReturn(Map.of(FIRST_ARGUMENT_NAME, -1, OFFSET_ARGUMENT_NAME, 20));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'first' is not allowed to be lower than 0."));
  }

  @Test
  void get_throwsException_forInvalidTooHighOffsetArgumentValue() {
    when(dataFetchingEnvironment.getArguments())
        .thenReturn(Map.of(FIRST_ARGUMENT_NAME, 2, OFFSET_ARGUMENT_NAME, 10001));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'offset' is not allowed to be higher than 10000."));
  }

  @Test
  void get_throwsException_forInvalidNegativeOffsetArgumentValue() {
    when(dataFetchingEnvironment.getArguments())
        .thenReturn(Map.of(FIRST_ARGUMENT_NAME, 2, OFFSET_ARGUMENT_NAME, -1));

    RequestValidationException exception =
        assertThrows(RequestValidationException.class, () -> connectionDataFetcher.get(dataFetchingEnvironment));
    assertThat(exception.getMessage(), is("Argument 'offset' is not allowed to be lower than 0."));
  }
}
