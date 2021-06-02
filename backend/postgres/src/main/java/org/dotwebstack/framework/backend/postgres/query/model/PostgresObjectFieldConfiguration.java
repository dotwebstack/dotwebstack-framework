package org.dotwebstack.framework.backend.postgres.query.model;

import static org.dotwebstack.framework.backend.postgres.query.model.Origin.REQUESTED;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;

@Data
@SuperBuilder
@Deprecated
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectFieldConfiguration extends ObjectFieldConfiguration {
  @Builder.Default
  private Origin origin = REQUESTED;

  private final PostgresObjectRequest postgresObjectRequest;

}
