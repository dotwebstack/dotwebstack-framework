package org.dotwebstack.framework.backend.postgres.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jooq.Record;
import org.jooq.Table;

@Data
@AllArgsConstructor
public class CrossJoin {
  @NonNull
  private Table<Record> fromTable;

  @NonNull
  private String columnName;

  private String alias;
}
