package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QueryPagingTest {

  private static QueryProperties.Paging paging;

  @BeforeAll
  static void setup() {
    paging = new QueryProperties.Paging();
    paging.setPage("$query.page");
    paging.setPageSize("$query.pageSize");
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPagingParametersNotProvided() {
    Map<String, Object> parameters = Map.of();

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(invalidConfigurationException.getMessage(),
        is("`page` and `pageSize` parameters are required for paging. Default values should be configured."));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPageSizeSmallerThanOneProvided() {
    Map<String, Object> parameters = Map.of("pageSize", 0, "page", 1);

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(),
        is("`pageSize` parameter value should be 1 or higher, but was 0."));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenNonIntegerPageSizeProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "10", "page", "1");

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(invalidConfigurationException.getMessage(),
        is("`pageSize` parameter must be configured having type integer."));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenNonIntegerPageProvided() {
    Map<String, Object> parameters = Map.of("pageSize", 10, "page", "1");

    InvalidConfigurationException invalidConfigurationException =
        assertThrows(InvalidConfigurationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(invalidConfigurationException.getMessage(),
        is("`page` parameter must be configured having type integer."));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPageSmallerThanOneProvided() {
    Map<String, Object> parameters = Map.of("pageSize", 10, "page", 0);

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(),
        is("`page` parameter value should be 1 or higher, but was 0."));
  }

  @Test
  void toPagingArguments_suppliesCorrectFirstAndOffsetOnFirstPage_forPageSize() {
    Map<String, Object> parameters = Map.of("pageSize", 42, "page", 1);

    Map<String, Integer> arguments = QueryPaging.toPagingArguments(paging, parameters);

    assertThat(arguments.get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(arguments.get(OFFSET_FIELD_NAME), is(0));
  }

  @Test
  void toPagingArguments_suppliesCorrectFirstAndOffsetOnFirstPage_forBigIntegerPageAndPageSize() {
    Map<String, Object> parameters = Map.of("pageSize", BigInteger.valueOf(42), "page", BigInteger.valueOf(1));

    Map<String, Integer> arguments = QueryPaging.toPagingArguments(paging, parameters);

    assertThat(arguments.get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(arguments.get(OFFSET_FIELD_NAME), is(0));
  }

  @Test
  void toPagingArguments_suppliesCorrectFirstAndOffset_forPageAndPageSize() {
    Map<String, Object> parameters = Map.of("pageSize", 42, "page", 3);

    Map<String, Integer> arguments = QueryPaging.toPagingArguments(paging, parameters);

    assertThat(arguments.get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(arguments.get(OFFSET_FIELD_NAME), is(84));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenMaxFirstIsExceeded() {
    Map<String, Object> parameters = Map.of("pageSize", 101, "page", 1);

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(), is("`pageSize` parameter value exceeds allowed value."));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenMaxOffsetIsExceeded() {
    Map<String, Object> parameters = Map.of("pageSize", 50, "page", 1001);

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(), is("`page` parameter value exceeds allowed value."));
  }
}
