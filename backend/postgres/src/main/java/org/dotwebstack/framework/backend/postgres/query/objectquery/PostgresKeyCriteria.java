package org.dotwebstack.framework.backend.postgres.query.objectquery;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.core.query.model.KeyCriteria;

@SuperBuilder
@Data
public class PostgresKeyCriteria extends KeyCriteria {
  private JoinTable joinTable;
}
