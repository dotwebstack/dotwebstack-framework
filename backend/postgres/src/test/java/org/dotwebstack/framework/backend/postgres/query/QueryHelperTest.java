package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createTableCreator;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class QueryHelperTest {

  @Test
  void findTable_returnsValue_forContextCriteria() {
    var context = new Context();

    var contextFields = new LinkedHashMap<String, ContextField>();
    contextFields.put("arg1", mock(ContextField.class));
    contextFields.put("arg2", mock(ContextField.class));

    context.setFields(contextFields);

    var contextCriteria = ContextCriteria.builder()
        .name("test")
        .context(context)
        .values(Map.of("arg2", "val2", "arg1", "val1"))
        .build();

    Table<Record> result = findTable("table", contextCriteria);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("table_test_ctx('val1','val2')"));
  }

  @Test
  void findTable_returnsValue_forDefault() {
    Table<Record> result = findTable("table", null);

    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo("table"));
  }

  @Test
  void createTableCreator_returnValueFromFunction_forSelect() {
    SelectQuery<?> query = mock(SelectQuery.class);

    AliasManager aliasManager = mock(AliasManager.class);

    var aliasName = "x1";

    when(aliasManager.newAlias()).thenReturn(aliasName);

    Function<String, Table<Record>> function = createTableCreator(query, null, aliasManager);

    assertThat(function, notNullValue());

    var result = function.apply(aliasName);
    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo(aliasName));

    verify(query, times(1)).addFrom(ArgumentMatchers.any(Table.class));
  }

  @Test
  void getFieldValue_returnsValue_forStringField() {
    var field = new PostgresObjectField();
    var fieldValue = QueryHelper.getFieldValue(field, "foo");
    assertThat(fieldValue.toString(), equalTo("'foo'"));
  }

  @Test
  void getFieldValue_returnsCastedValue_forStringEnumField() {
    var enumConfig = new FieldEnumConfiguration();
    enumConfig.setType("foo_type");
    var field = new PostgresObjectField();
    field.setEnumeration(enumConfig);
    var fieldValue = QueryHelper.getFieldValue(field, "foo");
    assertThat(fieldValue.toString(), equalTo("cast('foo' as foo_type)"));
  }
}
