package org.dotwebstack.framework.backend.postgres.query;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class QueryHelper {

  public static Field<Object> field(Table<Record> table, String fieldName, String alias) {
    Field<Object> field = field(table, fieldName);
    return field.as(alias);
  }

  public static Field<Object> field(Table<Record> table, String fieldName) {
    String qualifiedName = table.getName()
        .concat(".")
        .concat(fieldName);
    return DSL.field(qualifiedName);
  }
}
