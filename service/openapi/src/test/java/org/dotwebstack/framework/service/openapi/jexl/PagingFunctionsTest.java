package org.dotwebstack.framework.service.openapi.jexl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PagingFunctionsTest {

  private static PagingFunctions pagingFunctions;

  @BeforeAll
  static void beforeAll() {
    pagingFunctions = new PagingFunctions();
  }

  static Stream<Arguments> argumentsNext() {
    return Stream.of(Arguments.of("3", "http://dotwebstack.com/breweries", "http://dotwebstack.com/breweries?page=2"),
        Arguments.of("3", "http://dotwebstack.com/breweries?page=5", "http://dotwebstack.com/breweries?page=6"),
        Arguments.of("3", "http://dotwebstack.com/breweries?foo=bar&page=5",
            "http://dotwebstack.com/breweries?foo=bar&page=6"),
        Arguments.of("4", "http://dotwebstack.com/breweries?page=5", null));
  }

  static Stream<Arguments> argumentsPrev() {
    return Stream.of(Arguments.of("http://dotwebstack.com/breweries", null),
        Arguments.of("http://dotwebstack.com/breweries?page=1", null),
        Arguments.of("http://dotwebstack.com/breweries?page=7", "http://dotwebstack.com/breweries?page=6"), Arguments
            .of("http://dotwebstack.com/breweries?foo=bar&page=7", "http://dotwebstack.com/breweries?foo=bar&page=6"));
  }

  @Test
  void namespace_returnsCorrectly() {
    String namespace = pagingFunctions.getNamespace();

    assertThat(namespace, is("paging"));
  }

  @ParameterizedTest
  @MethodSource("argumentsNext")
  void next_returnsCorrectPageUri(String pageSize, String requestUri, String expected) {
    var data = Map.of("nodes", List.of("a", "b", "c"));

    String next = pagingFunctions.next(data, pageSize, requestUri);

    assertThat(next, is(expected));
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

  @ParameterizedTest
  @MethodSource("argumentsPrev")
  void prev_returnsCorrectPageUri(String requestUri, String expected) {
    String prev = pagingFunctions.prev(requestUri);

    assertThat(prev, is(expected));
  }
}
