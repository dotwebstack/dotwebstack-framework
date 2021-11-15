package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
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

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(), is("pageSize parameter value not provided"));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPageSizeSmallerThanOneProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "0", "page", "1");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(),
        is("pageSize parameter value should be 1 or higher, but was 0"));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenNonIntegerPageSizeProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "10.5", "page", "1");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(),
        is("pageSize parameter value should be an integer 1 or higher, but was 10.5"));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPageParameterProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "10");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(), is("page parameter value not provided"));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenPageSmallerThanOneProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "10", "page", "0");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(), is("page parameter value should be 1 or higher, but was 0"));
  }

  @Test
  void toPagingArguments_throwsParameterValidationException_whenNonIntegerPageProvided() {
    Map<String, Object> parameters = Map.of("pageSize", "10", "page", "foo");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> QueryPaging.toPagingArguments(paging, parameters));

    assertThat(parameterValidationException.getMessage(),
        is("page parameter value should be an integer 1 or higher, but was foo"));
  }

  @Test
  void toPagingArguments_suppliesCorrectFirstAndOffsetOnFirstPage_forPageSize() {
    Map<String, Object> parameters = Map.of("pageSize", "42", "page", "1");

    Map<String, Integer> arguments = QueryPaging.toPagingArguments(paging, parameters);

    assertThat(arguments.get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(arguments.get(OFFSET_FIELD_NAME), is(0));
  }

  @Test
  void toPagingArguments_suppliesCorrectFirstAndOffset_forPageAndPageSize() {
    Map<String, Object> parameters = Map.of("pageSize", "42", "page", "3");

    Map<String, Integer> arguments = QueryPaging.toPagingArguments(paging, parameters);

    assertThat(arguments.get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(arguments.get(OFFSET_FIELD_NAME), is(84));
  }
}
