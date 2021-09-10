package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.jooq.Table;

@Builder
@Getter
class PostgresTableField {
  private final PostgresFieldConfiguration fieldConfiguration;

  private final Table<?> table;
}
