package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.MatcherAssert.assertThat;

import io.r2dbc.postgresql.client.SSLMode;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class PostgresPropertiesTest {

  @Test
  void initObject_returnsProperties() {

    var resultObject = new PostgresProperties();

    assertThat(resultObject.getPool()
        .getInitialSize(), CoreMatchers.is(10));
    assertThat(resultObject.getPool()
        .getMaxSize(), CoreMatchers.is(100));
    assertThat(resultObject.getPool()
        .getMaxIdleTime(), CoreMatchers.is(30));
    assertThat(resultObject.getSslMode(), CoreMatchers.is(SSLMode.DISABLE));
    assertThat(resultObject.getDatabase(), CoreMatchers.is("postgres"));
    assertThat(resultObject.getPassword(), CoreMatchers.is("postgres"));
    assertThat(resultObject.getUsername(), CoreMatchers.is("postgres"));
    assertThat(resultObject.getPort(), CoreMatchers.is(5432));
    assertThat(resultObject.getHost(), CoreMatchers.is("localhost"));
  }
}
