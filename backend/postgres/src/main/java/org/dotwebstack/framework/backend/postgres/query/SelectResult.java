package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;

@Builder
@Getter
public class SelectResult {
  private SelectFieldOrAsterisk selectFieldOrAsterisk;

  private SelectQuery<Record> selectQuery;
}
