package org.dotwebstack.framework.backend.postgres.query;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.datafetchers.GenericDataFetcher;

public class QueryUtil {

  private QueryUtil() {}

  public static UnaryOperator<Map<String, Object>> createMapAssembler(SelectContext selectContext) {

    return row -> {
      if (!StringUtils.isEmpty(selectContext.getCheckNullAlias()
          .get()) && row.get(
              selectContext.getCheckNullAlias()
                  .get()) == null) {
        if (selectContext.getQueryContext()
            .isUseNullMapWhenNotFound()) {
          return GenericDataFetcher.NULL_MAP;
        }
        return null;
      }

      return selectContext.getAssembleFns()
          .entrySet()
          .stream()
          .collect(HashMap::new, (acc, entry) -> acc.put(entry.getKey(), entry.getValue()
              .apply(row)), HashMap::putAll);
    };
  }
}
