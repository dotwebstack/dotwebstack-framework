package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

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

  @Test
  void createJoinConditions_returnsListCondition() {
    var contextCriteria = ContextCriteria.builder()
        .name("test")
        .context(mock(Context.class))
        .values(Map.of("arg", "val"))
        .build();
    Table<Record> junctionTable = findTable("table1", contextCriteria);
    Table<Record> referencedTable = findTable("table2", contextCriteria);
    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getName()).thenReturn("arg");
    List<JoinColumn> joinColumns = List.of(joinColumn);
    when(joinColumn.getReferencedField()).thenReturn("any");
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    PostgresObjectField field = mock(PostgresObjectField.class);
    when(field.getColumn()).thenReturn("arg");
    when(objectType.getField(any(String.class))).thenReturn(field);

    var result = createJoinConditions(junctionTable, referencedTable, joinColumns, objectType);
    assertThat(result.size(), is(1));
    assertThat(result.get(0)
        .toString(), is("\"table1_test_ctx({0})\".\"arg\" = \"table2_test_ctx({0})\".\"arg\""));
  }

  @Test
  void createTableCreator_returnValueFromFunction_forSelect() {
    SelectQuery<?> query = mock(SelectQuery.class);

    Function<String, Table<Record>> function = createTableCreator(query, null);

    assertThat(function, notNullValue());

    var tableName = "foo";

    var result = function.apply(tableName);
    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo(tableName));

    verify(query, times(1)).addFrom(ArgumentMatchers.any(Table.class));
  }
}
