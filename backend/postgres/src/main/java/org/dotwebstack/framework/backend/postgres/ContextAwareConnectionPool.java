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
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Slf4j
public class ContextAwareConnectionPool implements ConnectionFactory {

  public static final String CTX_CONNECTION_UUID = "CONNECTION_UUID";

  private final Map<String, Connection> connectionMap = new HashMap<>();

  private final ConnectionPool connectionPool;

  public ContextAwareConnectionPool(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  @Override
  public Publisher<? extends Connection> create() {
    return Mono.deferContextual(ctx -> connectionPool.create()
        .doOnNext(connection -> addToConnectionMap(ctx, connection)));
  }

  private void addToConnectionMap(ContextView ctx, Connection connection) {
    if (ctx.hasKey(CTX_CONNECTION_UUID)) {
      connectionMap.put(ctx.get(CTX_CONNECTION_UUID), connection);
    } else {
      LOG.debug("Context does not contain key: {}", CTX_CONNECTION_UUID);
    }
  }

  public void cancelRequest(String uuid) {
    LOG.debug("Cancel connection for uuid {} (exists: {})", uuid, connectionMap.containsKey(uuid));
    getConnection(uuid).filter(Wrapped.class::isInstance)
        .map(Wrapped.class::cast)
        .map(Wrapped::unwrap)
        .filter(PostgresqlConnection.class::isInstance)
        .map(PostgresqlConnection.class::cast)
        .ifPresent(this::cancelRequest);
  }

  private void cancelRequest(PostgresqlConnection conn) {
    conn.cancelRequest()
        .subscribe();
  }

  public void cleanUp(String uuid) {
    LOG.debug("Clean up connection for uuid {} (exists: {})", uuid, connectionMap.containsKey(uuid));
    connectionMap.remove(uuid);
  }

  private Optional<Connection> getConnection(String uuid) {
    if (connectionMap.containsKey(uuid)) {
      return Optional.of(connectionMap.get(uuid));
    }
    return Optional.empty();
  }

  boolean hasConnections() {
    return !connectionMap.isEmpty();
  }

  @Override
  public ConnectionFactoryMetadata getMetadata() {
    return connectionPool.getMetadata();
  }
}
