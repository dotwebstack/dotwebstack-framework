package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Getter;
import org.jooq.Query;

@Builder
@Getter
public class QueryHolder {

  private final Query query;

  private final UnaryOperator<Map<String, Object>> mapAssembler;

  private final Map<String, String> keyColumnNames;
}
