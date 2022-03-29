package org.dotwebstack.framework.backend.postgres;

import static org.dotwebstack.framework.backend.postgres.ContextAwareConnectionPool.CTX_CONNECTION_UUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Wrapped;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ContextAwareConnectionPoolTest {

  @Mock
  private PostgresqlConnection connection;

  @Mock
  private ConnectionPool connectionPool;

  @BeforeEach
  void init() {
    lenient().when(connection.cancelRequest())
        .thenReturn(Mono.empty());

    var pooledConnection = mock(PooledConnection.class);
    lenient().when(pooledConnection.unwrap())
        .thenReturn(connection);

    lenient().when(connectionPool.create())
        .thenReturn(Mono.just(pooledConnection));
  }

  @Test
  void cancelRequest_executed_whenContextIsPresent() {
    var uuid = UUID.randomUUID()
        .toString();

    ContextAwareConnectionPool contextAwareConnectionPool = new ContextAwareConnectionPool(connectionPool);

    Flux.from(contextAwareConnectionPool.create())
        .contextWrite(ctx -> ctx.put(CTX_CONNECTION_UUID, uuid))
        .subscribe();
    contextAwareConnectionPool.cancelRequest(uuid);
    contextAwareConnectionPool.cleanUp(uuid);

    verify(connection, times(1)).cancelRequest();
    assertThat(contextAwareConnectionPool.hasConnections(), is(false));
  }

  @Test
  void cancelRequest_notExecuted_whenContextIsMissing() {
    var uuid = UUID.randomUUID()
        .toString();

    ContextAwareConnectionPool contextAwareConnectionPool = new ContextAwareConnectionPool(connectionPool);

    Flux.from(contextAwareConnectionPool.create())
        .subscribe();
    contextAwareConnectionPool.cancelRequest(uuid);
    contextAwareConnectionPool.cleanUp(uuid);

    verify(connection, never()).cancelRequest();
    assertThat(contextAwareConnectionPool.hasConnections(), is(false));
  }

  private abstract static class PooledConnection implements Wrapped<PostgresqlConnection>, Connection {
  }
}
