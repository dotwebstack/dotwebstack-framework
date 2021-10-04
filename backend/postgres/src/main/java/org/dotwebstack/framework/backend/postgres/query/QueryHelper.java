package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.datafetchers.GenericDataFetcher;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class QueryHelper {

  private QueryHelper() {}

  public static Field<Object> field(Table<?> table, String columnName) {
    return DSL.field(DSL.name(table.getName(), columnName));
  }

  public static UnaryOperator<Map<String, Object>> createMapAssembler(
      Map<String, Function<Map<String, Object>, Object>> assembleFns, AtomicReference<String> checkNullAlias,
      boolean isUseNullMapWhenNotFound) {
    return row -> {
      if (StringUtils.isNotEmpty(checkNullAlias.get()) && row.get(checkNullAlias.get()) == null) {
        if (isUseNullMapWhenNotFound) {
          return GenericDataFetcher.NULL_MAP;
        }
        return null;
      }

      return assembleFns.entrySet()
          .stream()
          .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
              .apply(row)), HashMap::putAll);
    };
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
