package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PostgresObjectFieldTest {

  @Test
  void getColumn_returnsColumn_whenColumnSet() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setColumn("foo_bar");

    var result = objectField.getColumn();

    assertThat(result, is("foo_bar"));
  }

  @ParameterizedTest
  @CsvSource({"foo, foo", "fooBar, foo_bar", "fooBarBaz, foo_bar_baz", "fooBAR, foo_bar", "fooBarBAZ, foo_bar_baz",
      "fooBARBaz, foo_bar_baz", "FOOBarBaz, foo_bar_baz"})
  void getColumn_returnsGeneratedColumn_whenColumnUnset(String name, String column) {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName(name);

    var result = objectField.getColumn();

    assertThat(result, is(column));
  }
}
