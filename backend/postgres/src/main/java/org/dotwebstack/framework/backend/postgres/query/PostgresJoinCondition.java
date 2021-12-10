package org.dotwebstack.framework.backend.postgres.query;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.core.query.model.JoinCondition;


@SuperBuilder
@Getter
public class PostgresJoinCondition extends JoinCondition {
  private JoinTable joinTable;
}
