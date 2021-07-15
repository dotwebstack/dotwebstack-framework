package org.dotwebstack.framework.backend.postgres.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.datafetchers.GenericDataFetcher;
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
}
