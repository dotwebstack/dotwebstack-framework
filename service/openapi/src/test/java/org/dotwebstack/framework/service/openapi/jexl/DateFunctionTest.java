package org.dotwebstack.framework.service.openapi.jexl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DateFunctionTest {

  private static DateFunction dateFunction;

  @BeforeAll
  static void beforeAll() {
    dateFunction = new DateFunction();
  }

  @Test
  void namespace_returnsCorrectly() {
    String namespace = dateFunction.getNamespace();

    assertThat(namespace, is("date"));
  }

  @Test
  void currentDate_returnsValidDate() {
    String date = dateFunction.currentDate();

    assertThat(date, matchesPattern("20[0-9][0-9]-[0-9][0-9]-[0-9][0-9]"));
  }
}
