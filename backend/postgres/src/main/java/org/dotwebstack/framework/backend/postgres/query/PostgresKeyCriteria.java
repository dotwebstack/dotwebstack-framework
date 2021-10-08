package org.dotwebstack.framework.backend.postgres.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.core.query.model.KeyCriteria;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class PostgresKeyCriteria extends KeyCriteria {
  private JoinTable joinTable;
}
