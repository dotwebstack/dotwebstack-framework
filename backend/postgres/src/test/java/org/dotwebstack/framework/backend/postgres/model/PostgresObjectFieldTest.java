package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class PostgresObjectFieldTest {

  @Test
  void getColumn_returnsColumn() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setColumn("bbb");

    var result = objectField.getColumn();

    assertThat(result, is("bbb"));
  }

  @Test
  void getColumn_returnsColumn_forName() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("aaa");

    var result = objectField.getColumn();

    assertThat(result, is("aaa"));
  }

  @Test
  void getColumn_returnsColumn_forCamelCasedName() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("fooBar");

    var result = objectField.getColumn();

    assertThat(result, is("foo_bar"));
  }

  @Test
  void getColumn_returnsColumn_forNameWithUpperAbbrev() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("fooBAR");

    var result = objectField.getColumn();

    assertThat(result, is("foo_bar"));
  }
}
