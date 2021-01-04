package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;

@Builder
@Getter
public final class SelectQuery {

  private static final Logger LOG = LoggerFactory.getLogger(SelectQuery.class);

  private final VarBinder varBinder = new VarBinder();

  @NonNull
  private final Table fromTable;

  private final WhereCondition whereCondition;

  public DatabaseClient.GenericExecuteSpec execute(DatabaseClient databaseClient) {
    String querySql = toSql();
    LOG.debug("PostgreSQL query: {}", querySql);

    return varBinder.bind(databaseClient.sql(querySql));
  }

  public String toSql() {
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append(String.format("SELECT * FROM %s", fromTable.toSql(varBinder)));

    if (whereCondition != null) {
      sqlBuilder.append(" WHERE ")
          .append(whereCondition.toSQL(varBinder));
    }

    sqlBuilder.append(" LIMIT 100");

    return sqlBuilder.toString();
  }
}
