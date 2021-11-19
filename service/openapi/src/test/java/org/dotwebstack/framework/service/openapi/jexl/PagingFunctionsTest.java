package org.dotwebstack.framework.service.openapi.jexl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PagingFunctionsTest {

  private static PagingFunctions pagingFunctions;

  @BeforeAll
  static void beforeAll() {
    pagingFunctions = new PagingFunctions();
  }

  @Test
  void namespace_returnsCorrectly() {
    String namespace = pagingFunctions.getNamespace();

    assertThat(namespace, is("paging"));
  }

  @Test
  void next_givenPagelessRequestUri_returnsSecondPageUri() {
    var data = Map.of("nodes", List.of("a", "b", "c"));
    var pageSize = "3";
    var requestUri = "http://dotwebstack.com/breweries";

    String next = pagingFunctions.next(data, pageSize, requestUri);

    assertThat(next, is("http://dotwebstack.com/breweries?page=2"));
  }

  @Test
  void next_givenRequestUriWithPage_returnsNextPageUri() {
    var data = Map.of("nodes", List.of("a", "b", "c"));
    var pageSize = "3";
    var requestUri = "http://dotwebstack.com/breweries?page=5";

    String next = pagingFunctions.next(data, pageSize, requestUri);

    assertThat(next, is("http://dotwebstack.com/breweries?page=6"));
  }

  @Test
  void next_givenRequestUriWithOtherParamsAndPage_returnsNextPageUri() {
    var data = Map.of("nodes", List.of("a", "b", "c"));
    var pageSize = "3";
    var requestUri = "http://dotwebstack.com/breweries?foo=bar&page=5";

    String next = pagingFunctions.next(data, pageSize, requestUri);

    assertThat(next, is("http://dotwebstack.com/breweries?foo=bar&page=6"));
  }

  @Test
  void next_givenRequestUriWithPageAndHigherPageSize_returnsNull() {
    var data = Map.of("nodes", List.of("a", "b", "c"));
    var pageSize = "4";
    var requestUri = "http://dotwebstack.com/breweries?page=5";

    String next = pagingFunctions.next(data, pageSize, requestUri);

    assertThat(next, nullValue());
  }

  @Test
  void next_givenUnPageableData_throwsException() {
    var data = Map.of("foo", "a", "b", "c");
    var pageSize = "4";
    var requestUri = "http://dotwebstack.com/breweries?page=5";

    InvalidConfigurationException exception =
        assertThrows(InvalidConfigurationException.class, () -> pagingFunctions.next(data, pageSize, requestUri));

    assertThat(exception.getMessage(), is("paging:next JEXL function used on un-pageable field"));
  }

  @Test
  void prev_givenPagelessRequestUri_returnsNull() {
    var requestUri = "http://dotwebstack.com/breweries";

    String prev = pagingFunctions.prev(requestUri);

    assertThat(prev, nullValue());
  }

  @Test
  void prev_givenRequestUriWithFirstPage_returnsNull() {
    var requestUri = "http://dotwebstack.com/breweries?page=1";

    String prev = pagingFunctions.prev(requestUri);

    assertThat(prev, nullValue());
  }

  @Test
  void prev_givenRequestUriWithPage_returnsPrevPage() {
    var requestUri = "http://dotwebstack.com/breweries?page=7";

    String prev = pagingFunctions.prev(requestUri);

    assertThat(prev, is("http://dotwebstack.com/breweries?page=6"));
  }

  @Test
  void prev_givenRequestUriWithOtherParamsAndPage_returnsPrevPage() {
    var requestUri = "http://dotwebstack.com/breweries?foo=bar&page=7";

    String prev = pagingFunctions.prev(requestUri);

    assertThat(prev, is("http://dotwebstack.com/breweries?foo=bar&page=6"));
  }
}
