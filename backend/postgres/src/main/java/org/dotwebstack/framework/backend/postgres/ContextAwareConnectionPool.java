package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.Wrapped;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

@Slf4j
public class ContextAwareConnectionPool implements ConnectionFactory {
  private final Map<Thread, Connection> connectionMap = new HashMap<>();

  private final ConnectionPool connectionPool;

  public ContextAwareConnectionPool(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public Publisher<? extends Connection> create() {
    Thread requestThread = Thread.currentThread();
    return connectionPool.create()
        .doOnNext(connection -> connectionMap.put(requestThread, connection));
  }

  public void cancelRequest(Thread requestThread) {
    LOG.debug("Cancel connection for thread {} (exists: {})", requestThread, connectionMap.containsKey(requestThread));
    getConnection(requestThread).filter(Wrapped.class::isInstance)
        .map(Wrapped.class::cast)
        .map(Wrapped::unwrap)
        .filter(PostgresqlConnection.class::isInstance)
        .map(PostgresqlConnection.class::cast)
        .ifPresent(conn -> cancelRequest(conn, requestThread));
  }

  private void cancelRequest(PostgresqlConnection conn, Thread requestThread) {
    conn.cancelRequest()
        .doOnTerminate(() -> cleanUp(requestThread))
        .subscribe();
  }

  public void cleanUp(Thread requestThread) {
    LOG.debug("Clean up connection for thread {} (exists: {})", requestThread,
        connectionMap.containsKey(requestThread));
    connectionMap.remove(requestThread);
  }

  private Optional<Connection> getConnection(Thread requestThread) {
    if (connectionMap.containsKey(requestThread)) {
      return Optional.of(connectionMap.get(requestThread));
    }
    return Optional.empty();
  }

  @Override
  public ConnectionFactoryMetadata getMetadata() {
    return connectionPool.getMetadata();
  }
}
