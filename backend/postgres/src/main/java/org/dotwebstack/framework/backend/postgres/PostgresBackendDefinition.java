package org.dotwebstack.framework.backend.postgres;

import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.backend.BackendDefinition;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.springframework.stereotype.Component;

@Component
class PostgresBackendDefinition implements BackendDefinition {

  @Override
  public Class<? extends AbstractTypeConfiguration<?>> getTypeConfigurationClass() {
    return PostgresTypeConfiguration.class;
  }
}
