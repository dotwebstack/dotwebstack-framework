package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration.Builder;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import io.r2dbc.spi.ConnectionFactory;
import java.util.Collection;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
@EnableConfigurationProperties(PostgresProperties.class)
public class PostgresConfiguration extends AbstractR2dbcConfiguration {

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
    return DSL.using(SQLDialect.POSTGRES);
  }

  @Bean
  @Override
  public ConnectionFactory connectionFactory() {
    Builder configurationBuilder = PostgresqlConnectionConfiguration.builder()
        .host(postgresProperties.getHost())
        .port(postgresProperties.getPort())
        .username(postgresProperties.getUsername())
        .password(postgresProperties.getPassword());

    codecRegistrars.forEach(configurationBuilder::codecRegistrar);

    return new PostgresqlConnectionFactory(configurationBuilder.build());
  }
}
