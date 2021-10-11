package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.Table;
import org.junit.jupiter.api.Test;

class QueryHelperTest {

  @Test
  void findTable_returnsValue_forContextCriteria() {
    var contextCriteria = ContextCriteria.builder()
        .name("test")
        .context(mock(Context.class))
        .values(Map.of("arg", "val"))
        .build();

    Table<Record> result = findTable("table", contextCriteria);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("table_test_ctx('val')"));
  }

  @Test
  void findTable_returnsValue_forDefault() {
    Table<Record> result = findTable("table", null);

    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo("table"));
  }
}
