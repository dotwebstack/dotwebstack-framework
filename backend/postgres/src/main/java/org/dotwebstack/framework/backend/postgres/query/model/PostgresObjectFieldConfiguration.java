package org.dotwebstack.framework.backend.postgres.query.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;

import static org.dotwebstack.framework.backend.postgres.query.model.Origin.REQUESTED;

@Data
@SuperBuilder
@Deprecated
public class PostgresObjectFieldConfiguration extends ObjectFieldConfiguration {
   @Builder.Default
   private Origin origin = REQUESTED;

   private final PostgresObjectRequest postgresObjectRequest;

}
