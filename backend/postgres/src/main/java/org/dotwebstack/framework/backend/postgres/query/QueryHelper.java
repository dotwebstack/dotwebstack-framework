package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class QueryHelper {

  private QueryHelper() {}

  public static Field<Object> field(Table<?> table, String columnName) {
    return DSL.field(DSL.name(table.getName(), columnName));
  }

  public static PostgresObjectType getObjectType(ObjectRequest objectRequest) {
    var objectType = objectRequest.getObjectType();

    if (!(objectType instanceof PostgresObjectType)) {
      throw illegalArgumentException("Object type has wrong type.");
    }

    return (PostgresObjectType) objectType;
  }

  public static PostgresObjectField getObjectField(ObjectRequest objectRequest, String name) {
    return getObjectType(objectRequest).getField(name)
        .orElseThrow(() -> illegalArgumentException("Object field '{}' not found.", name));
  }
}
