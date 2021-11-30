package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
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
  public ConnectionFactory connectionFactory() {
    var configurationBuilder = PostgresqlConnectionConfiguration.builder()
        .host(postgresProperties.getHost())
        .port(postgresProperties.getPort())
        .username(postgresProperties.getUsername())
        .password(postgresProperties.getPassword())
        .sslMode(postgresProperties.getSslMode())
        .forceBinary(false); // Disable due to R2DBC bug with numeric values

    if (postgresProperties.getOptions() != null) {
      configurationBuilder.options(postgresProperties.getOptions());
    }

    if (StringUtils.isNotEmpty(postgresProperties.getDatabase())) {
      configurationBuilder.database(postgresProperties.getDatabase());
    }

    codecRegistrars.forEach(configurationBuilder::codecRegistrar);

    var connectionFactory = new PostgresqlConnectionFactory(configurationBuilder.build());
    var poolProperties = postgresProperties.getPool();

    var poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
        .initialSize(poolProperties.getInitialSize())
        .maxSize(poolProperties.getMaxSize())
        .maxIdleTime(Duration.ofMinutes(poolProperties.getMaxIdleTime()))
        .build();

    return new ConnectionPool(poolConfiguration);
  }

  @Bean
  public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
    return DatabaseClient.create(connectionFactory);
  }
}
