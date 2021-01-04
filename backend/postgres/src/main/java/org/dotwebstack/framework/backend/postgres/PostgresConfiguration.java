package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
@EnableConfigurationProperties(PostgresProperties.class)
public class PostgresConfiguration extends AbstractR2dbcConfiguration {

  private final PostgresProperties postgresProperties;

  public PostgresConfiguration(PostgresProperties postgresProperties) {
    this.postgresProperties = postgresProperties;
  }

  @Bean
  @Override
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        .host(postgresProperties.getHost())
        .port(postgresProperties.getPort())
        .username(postgresProperties.getUsername())
        .password(postgresProperties.getPassword())
        .build());
  }

  @Bean
  public TableRegistry tableMapping() {
    TableRegistry tableRegistry = new TableRegistry();

    postgresProperties.getTypeMapping()
        .forEach((typeName, typeConfiguration) -> {
          tableRegistry.register(typeName, TableRegistry.TableMapping.builder()
              .name(typeConfiguration.getTable())
              .keyColumn(postgresProperties.getKeyColumn())
              .build());
        });

    return tableRegistry;
  }
}
