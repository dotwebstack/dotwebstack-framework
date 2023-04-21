package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;

@Builder
@Getter
public class SelectResult {

  private String objectName;

  private Field<?> selectField;

  private SelectQuery<Record> selectQuery;
}
