package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import io.r2dbc.postgresql.client.SSLMode;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class PostgresConfigurationTest {
  
  @Mock
  private PostgresProperties postgresProperties;
  @Mock
  private Collection<CodecRegistrar> codecRegistrars;

  private PostgresConfiguration postgresConfiguration;
  
  @BeforeEach
  void doBeforeEach() {
    postgresConfiguration = new PostgresConfiguration(postgresProperties, codecRegistrars);
  }
  
  @Test
  void dslContext_shouldReturn_DslContext() {
    DSLContext result = postgresConfiguration.dslContext();
  
    String propertyResult = System.getProperties().getProperty("org.jooq.no-logo");
  
    assertThat(propertyResult, is(notNullValue()));
    assertTrue(Boolean.parseBoolean(propertyResult));
    assertTrue(result instanceof DSLContext);
  }
  
  @Test
  void connectionFactory_shouldReturn_ConnectionFactory() {
    doReturn("a").when(postgresProperties).getHost();
    doReturn(80).when(postgresProperties).getPort();
    doReturn("aaa").when(postgresProperties).getUsername();
    doReturn("bbb").when(postgresProperties).getPassword();
    Map<String, String> options = new HashMap<>();
    options.put("z", "f");
    doReturn(SSLMode.ALLOW).when(postgresProperties).getSslMode();
    doReturn("anyDB").when(postgresProperties).getDatabase();
    PostgresProperties.Pool pool = new PostgresProperties.Pool();
    pool.setInitialSize(100);
    pool.setMaxSize(1000);
    pool.setMaxIdleTime(10);
    doReturn(pool).when(postgresProperties).getPool();
    
    var result = postgresConfiguration.connectionFactory();
  
    assertThat(result, is(notNullValue()));
    assertTrue(result instanceof ConnectionFactory);
    assertTrue(result.getMetadata() instanceof ConnectionFactoryMetadata);
  }
  
  @Test
  void databaseClient_shouldReturn_DatabaseClient() {
    doReturn("a").when(postgresProperties).getHost();
    doReturn(80).when(postgresProperties).getPort();
    doReturn("aaa").when(postgresProperties).getUsername();
    doReturn("bbb").when(postgresProperties).getPassword();
    Map<String, String> options = new HashMap<>();
    options.put("z", "f");
    doReturn(SSLMode.ALLOW).when(postgresProperties).getSslMode();
    doReturn("anyDB").when(postgresProperties).getDatabase();
    PostgresProperties.Pool pool = new PostgresProperties.Pool();
    pool.setInitialSize(100);
    pool.setMaxSize(1000);
    pool.setMaxIdleTime(10);
    doReturn(pool).when(postgresProperties).getPool();
    
    var connectionFactory = postgresConfiguration.connectionFactory();
    var result = postgresConfiguration.databaseClient(connectionFactory);
  
    assertThat(result, is(notNullValue()));
    assertTrue(result instanceof DatabaseClient);
  }
}
