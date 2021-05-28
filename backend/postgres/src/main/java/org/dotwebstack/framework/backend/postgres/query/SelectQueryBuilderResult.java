package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jooq.SelectQuery;
import org.jooq.Table;


@Data
@Builder
public class SelectQueryBuilderResult {
  @NonNull
  private final SelectQuery<?> query;

  @NonNull
  private final Table<?> table;

  @NonNull
  private final UnaryOperator<Map<String, Object>> mapAssembler;

  @NonNull
  private final ObjectSelectContext context;
}
