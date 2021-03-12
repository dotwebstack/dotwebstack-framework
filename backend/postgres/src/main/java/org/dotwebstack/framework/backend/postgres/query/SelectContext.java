package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Data;
import lombok.Getter;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.Table;

@Data
@Getter
public class SelectContext {
  private QueryContext queryContext;

  private List<SelectFieldOrAsterisk> selectColumns = new ArrayList<>();

  private List<Table<Record>> joinTables = new ArrayList<>();

  private Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

  private AtomicReference<String> checkNullAlias = new AtomicReference<>();

  public SelectContext(QueryContext queryContext) {
    this.queryContext = queryContext;
  }

  public void addField(SelectedField selectedField, Field<?> field) {
    addField(selectedField.getName(), field);
  }

  public void addField(String fieldName, Field<?> field) {
    getSelectColumns().add(field);
    getAssembleFns().put(fieldName, row -> row.get(field.getName()));
  }
}
