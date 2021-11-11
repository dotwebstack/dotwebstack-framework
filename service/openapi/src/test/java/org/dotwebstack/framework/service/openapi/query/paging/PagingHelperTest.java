package org.dotwebstack.framework.service.openapi.query.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryPaging;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagingHelperTest {

  @Mock
  private GraphQlQuery query;

  private QueryPaging paging = QueryPaging.builder()
      .pageParam("$query.page")
      .pageSizeParam("$query.pageSize")
      .build();

  @Test
  void addPaging_throwsParameterValidationException_whenPagingParametersNotProvided() {
    Map<String, Object> inputParams = Map.of();

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(), is("pageSize parameter value not provided"));
  }

  @Test
  void addPaging_throwsParameterValidationException_whenPageSizeSmallerThanOneProvided() {
    Map<String, Object> inputParams = Map.of("pageSize", "0", "page", "1");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(),
        is("pageSize parameter value should be 1 or higher, but was 0"));
  }

  @Test
  void addPaging_throwsParameterValidationException_whenNonIntegerPageSizeProvided() {
    Map<String, Object> inputParams = Map.of("pageSize", "10.5", "page", "1");

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(),
        is("pageSize parameter value should be an integer 1 or higher, but was 10.5"));
  }

  @Test
  void addPaging_throwsParameterValidationException_whenPageParameterProvided() {
    Map<String, Object> inputParams = Map.of("pageSize", "10");
    when(query.getField()).thenReturn(Field.builder()
        .arguments(new HashMap<>())
        .build());

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(), is("page parameter value not provided"));
  }

  @Test
  void addPaging_throwsParameterValidationException_whenPageSmallerThanOneProvided() {
    Map<String, Object> inputParams = Map.of("pageSize", "10", "page", "0");
    when(query.getField()).thenReturn(Field.builder()
        .arguments(new HashMap<>())
        .build());

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(), is("page parameter value should be 1 or higher, but was 0"));
  }

  @Test
  void addPaging_throwsParameterValidationException_whenNonIntegerPageProvided() {
    Map<String, Object> inputParams = Map.of("pageSize", "10", "page", "foo");
    when(query.getField()).thenReturn(Field.builder()
        .arguments(new HashMap<>())
        .build());

    ParameterValidationException parameterValidationException =
        assertThrows(ParameterValidationException.class, () -> PagingHelper.addPaging(query, paging, inputParams));

    assertThat(parameterValidationException.getMessage(),
        is("page parameter value should be an integer 1 or higher, but was foo"));
  }

  @Test
  void addPaging_suppliesCorrectFirstAndOffsetOnFirstPage_forPageSize() {
    Map<String, Object> inputParams = Map.of("pageSize", "42", "page", "1");
    when(query.getField()).thenReturn(Field.builder()
        .arguments(new HashMap<>())
        .build());

    PagingHelper.addPaging(query, paging, inputParams);

    assertThat(query.getField()
        .getArguments()
        .get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(query.getField()
        .getArguments()
        .get(OFFSET_FIELD_NAME), is(0));
  }

  @Test
  void addPaging_suppliesCorrectFirstAndOffset_forPageAndPageSize() {
    Map<String, Object> inputParams = Map.of("pageSize", "42", "page", "3");
    when(query.getField()).thenReturn(Field.builder()
        .arguments(new HashMap<>())
        .build());

    PagingHelper.addPaging(query, paging, inputParams);

    assertThat(query.getField()
        .getArguments()
        .get(FIRST_ARGUMENT_NAME), is(42));
    assertThat(query.getField()
        .getArguments()
        .get(OFFSET_FIELD_NAME), is(84));
  }
}
