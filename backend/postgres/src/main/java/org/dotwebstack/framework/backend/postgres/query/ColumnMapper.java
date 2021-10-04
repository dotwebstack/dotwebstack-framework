package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.jooq.Field;

class ColumnMapper implements ScalarFieldMapper<Map<String, Object>> {

  private final Field<Object> field;

  public ColumnMapper(Field<Object> field) {
    this.field = field;
  }

  @Override
  public String getAlias() {
    return field.getName();
  }

  @Override
  public Object apply(Map<String, Object> row) {
    return row.get(field.getName());
  }
}
