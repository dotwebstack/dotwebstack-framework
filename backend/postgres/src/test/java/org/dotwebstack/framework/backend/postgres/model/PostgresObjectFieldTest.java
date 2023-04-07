package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
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
      "fooBARBaz, foo_bar_baz", "fooBBaz, foo_b_baz", "FOOBarBaz, foo_bar_baz", "foo3D, foo_3d", "foo33D, foo_33d",
      "foo3DBarBaz, foo_3d_bar_baz"})
  void getColumn_returnsGeneratedColumn_whenColumnUnset(String name, String column) {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName(name);
    objectField.initColumns();

    var result = objectField.getColumn();

    assertThat(result, is(column));
  }

  @Test
  void getColumn_returnsGeneratedColumn_whenColumnUnsetWithAncestor() {
    var ancestor = new PostgresObjectField();
    ancestor.setName("foo");

    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("bar");
    objectField.initColumns(List.of(ancestor));

    var result = objectField.getColumn();

    assertThat(result, is("foo__bar"));
  }
}
