package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import org.jooq.SelectQuery;

@Data
@Builder
public class SelectQueryBuilderResult {
  private final SelectQuery<?> query;

  private final UnaryOperator<Map<String, Object>> mapAssembler;
}
