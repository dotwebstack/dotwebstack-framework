package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import org.jooq.SelectQuery;
import org.jooq.Table;

@Data
@Builder
public class SelectQueryBuilderResult {
  private final SelectQuery<?> query;

  private final Table<?> table;

  private final UnaryOperator<Map<String, Object>> mapAssembler;
}
