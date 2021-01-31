package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@EnableConfigurationProperties(PostgresProperties.class)
public class PostgresConfiguration {

  private final PostgresProperties postgresProperties;

  public PostgresConfiguration(PostgresProperties postgresProperties) {
    this.postgresProperties = postgresProperties;
  }

  @Bean
  public DSLContext dslContext() {
    System.getProperties()
        .setProperty("org.jooq.no-logo", "true");
    return DSL.using(SQLDialect.POSTGRES);
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        .host(postgresProperties.getHost())
        .port(postgresProperties.getPort())
        .username(postgresProperties.getUsername())
        .password(postgresProperties.getPassword())
        .build());
  }

  @Bean
  public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }
}
