package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringHelperTest {

  @ParameterizedTest
  @CsvSource({"foo, foo", "fooBar, foo_bar", "fooBarBaz, foo_bar_baz", "fooBAR, foo_bar", "fooBarBAZ, foo_bar_baz",
      "fooBARBaz, foo_bar_baz", "fooBBaz, foo_b_baz", "FOOBarBaz, foo_bar_baz", "foo3D, foo_3d", "foo33D, foo_33d",
      "foo3DBarBaz, foo_3d_bar_baz"})
  void toSnakeCase_returnsSnakeCase_forString(String str, String expectedResult) {
    assertThat(StringHelper.toSnakeCase(str), is(expectedResult));
  }

  @Test
  void toSnakeCase_returnsNull_forNullString() {
    assertThat(StringHelper.toSnakeCase(null), is(nullValue()));
  }
}
