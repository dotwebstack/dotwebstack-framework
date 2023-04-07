package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.junit.jupiter.api.Test;

class PostgresObjectTypeTest {

  private PostgresObjectType postgresObjectType = new PostgresObjectType();

  @Test
  void isNested_returnsTrue() {
    assertTrue(postgresObjectType.isNested());
  }

  @Test
  void isNested_returnsFalse() {
    postgresObjectType.setTable("@@@");

    assertFalse(postgresObjectType.isNested());
    assertThat(postgresObjectType.getTable(), is("@@@"));
  }

  @Test
  void equalsObjects_returnsTrue() {
    postgresObjectType.setTable("@@@");

    var result = new PostgresObjectType();
    result.setTable("@@@");

    assertThat(result, equalTo(postgresObjectType));
  }

  @Test
  void equalsObjects_returnsFalse() {
    postgresObjectType.setTable("@@@");

    var result = new PostgresObjectType();
    result.setTable("%%%");

    assertThat(result, not(equalTo(postgresObjectType)));
  }

  @Test
  void new_returnsCopy_forObjectType() {
    var objectType = new PostgresObjectType();
    objectType.setName("testObject");
    objectType.setDistinct(true);
    objectType.setFilters(Map.of("filter", new FilterConfiguration()));
    objectType.setSortableBy(Map.of("sort", List.of(new SortableByConfiguration())));

    var result = new PostgresObjectType(objectType, List.of());

    assertThat(result, equalTo(objectType));
  }
}
