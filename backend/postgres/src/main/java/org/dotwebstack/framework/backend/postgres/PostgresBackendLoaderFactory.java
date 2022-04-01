package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.spi.ConnectionFactory;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.stereotype.Component;

@Component
public class PostgresBackendLoaderFactory implements BackendLoaderFactory {

  private final ConnectionFactory connectionFactory;

  public PostgresBackendLoaderFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public <T extends ObjectType<?>> BackendLoader create(T objectType) {
    return new PostgresBackendLoader(connectionFactory);
  }
}
