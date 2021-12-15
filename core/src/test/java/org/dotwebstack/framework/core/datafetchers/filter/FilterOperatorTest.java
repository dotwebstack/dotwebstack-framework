package org.dotwebstack.framework.core.datafetchers.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class FilterOperatorTest {
  @Test
  void getFilterOperator_returnsFilterOperator_forContainsAllOf() {
    var operator = FilterOperator.getFilterOperator("containsAllOf");

    assertThat(operator, is(FilterOperator.CONTAINS_ALL_OF));
  }

  @Test
  void getFilterOperator_returnsFilterOperator_forContainsAnyOf() {
    var operator = FilterOperator.getFilterOperator("containsAnyOf");

    assertThat(operator, is(FilterOperator.CONTAINS_ANY_OF));
  }

  @Test
  void getFilterOperator_returnsFilterOperator_forEq() {
    var operator = FilterOperator.getFilterOperator("eq");

    assertThat(operator, is(FilterOperator.EQ));
  }

  @Test
  void getFilterOperator_returnsNull_forUnknown() {
    var operator = FilterOperator.getFilterOperator("unknown");

    assertThat(operator, nullValue());
  }
}
