package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import org.jooq.Query;

@Builder
@Getter
public class QueryHolder {

  private final Query query;

  private final Function<Map<String, Object>, Map<String, Object>> rowAssembler;

  private final Map<String, String> keyColumnNames;
}
