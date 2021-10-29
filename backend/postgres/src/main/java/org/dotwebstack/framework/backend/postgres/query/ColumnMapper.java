package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.jooq.Field;

class ColumnMapper implements ScalarFieldMapper<Map<String, Object>> {

  private final Field<Object> column;

  public ColumnMapper(Field<Object> column) {
    this.column = column;
  }

  public Field<Object> getColumn() {
    return column;
  }

  @Override
  public String getAlias() {
    return column.getName();
  }

  @Override
  public Object apply(Map<String, Object> row) {
    return row.get(column.getName());
  }
}
