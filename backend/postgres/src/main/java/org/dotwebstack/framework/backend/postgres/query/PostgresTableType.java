package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.jooq.Table;

@Builder
@Getter
class PostgresTableType {
  private final PostgresTypeConfiguration typeConfiguration;

  private final Table<?> table;
}
