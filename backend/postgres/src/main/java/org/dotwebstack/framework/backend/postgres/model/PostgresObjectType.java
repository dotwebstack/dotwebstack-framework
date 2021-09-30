package org.dotwebstack.framework.backend.postgres.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectType;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectType extends AbstractObjectType<PostgresObjectField> {

  private String table;
}
