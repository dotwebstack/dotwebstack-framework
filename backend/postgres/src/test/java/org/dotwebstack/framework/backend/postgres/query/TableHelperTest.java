package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.Table;
import org.junit.jupiter.api.Test;

class TableHelperTest {

  @Test
  void findTable_returnsValue_forContextCriteria() {
    Table<Record> result = TableHelper.findTable("table", List.of(ContextCriteria.builder()
        .field("arg")
        .value("val")
        .build()));

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("table_ctx('val')"));
  }

  @Test
  void findTable_returnsValue_forDefault() {
    Table<Record> result = TableHelper.findTable("table", List.of());

    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo("table"));
  }
}
