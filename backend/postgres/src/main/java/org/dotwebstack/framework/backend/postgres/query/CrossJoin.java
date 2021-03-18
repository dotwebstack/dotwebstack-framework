package org.dotwebstack.framework.backend.postgres.query;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.jooq.Table;

@Data
@RequiredArgsConstructor
public class CrossJoin {
  @NonNull
  private Table<Record> fromTable;

  @NonNull
  private String columnName;

  @NonNull
  private String alias;
}
