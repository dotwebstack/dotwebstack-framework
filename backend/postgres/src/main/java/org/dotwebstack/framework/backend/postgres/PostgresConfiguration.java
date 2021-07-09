package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import io.r2dbc.spi.ConnectionFactory;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@EnableConfigurationProperties(PostgresProperties.class)
public class PostgresConfiguration {

  private final PostgresProperties postgresProperties;

  private final Collection<CodecRegistrar> codecRegistrars;

  public PostgresConfiguration(PostgresProperties postgresProperties, Collection<CodecRegistrar> codecRegistrars) {
    this.postgresProperties = postgresProperties;
    this.codecRegistrars = codecRegistrars;
  }

  @Bean
  public DSLContext dslContext() {
    System.getProperties()
        .setProperty("org.jooq.no-logo", "true");

    return DSL.using(buildPostgresqlUrl(), postgresProperties.getUsername(), postgresProperties.getPassword());
  }

  private String buildPostgresqlUrl() {
    return String.format("jdbc:postgresql://%s:%d/%s", postgresProperties.getHost(), postgresProperties.getPort(),
        postgresProperties.getDatabase());
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    var configurationBuilder = PostgresqlConnectionConfiguration.builder()
        .host(postgresProperties.getHost())
        .port(postgresProperties.getPort())
        .username(postgresProperties.getUsername())
        .password(postgresProperties.getPassword())
        .options(postgresProperties.getOptions())
        .forceBinary(true);

    if (StringUtils.isNotEmpty(postgresProperties.getDatabase())) {
      configurationBuilder.database(postgresProperties.getDatabase());
    }

    codecRegistrars.forEach(configurationBuilder::codecRegistrar);

    return new PostgresqlConnectionFactory(configurationBuilder.build());
  }

  @Bean
  public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }
}
