package org.dotwebstack.framework.backend.postgres.query;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.jooq.Query;

@Builder
@Getter
public class QueryHolder {

  private final Query query;

  private final RowAssembler rowAssembler;

  private final Map<String, String> keyColumnNames;

  private final List<QueryHolder> joinTableQueries;
}
