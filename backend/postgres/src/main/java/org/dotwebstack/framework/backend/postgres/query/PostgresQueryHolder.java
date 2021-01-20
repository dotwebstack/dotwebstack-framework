package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.jooq.Query;

@Builder
@Getter
public class PostgresQueryHolder {

  private final Query query;

  private final Map<String, Object> fieldAliasMap;
}
