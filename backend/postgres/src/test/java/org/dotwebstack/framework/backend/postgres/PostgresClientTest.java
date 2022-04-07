package org.dotwebstack.framework.backend.postgres;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.postgresql.api.PostgresqlStatement;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Wrapped;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.BiFunction;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class PostgresClientTest {

  @Mock
  private ConnectionFactory connectionFactory;

  private PostgresClient postgresClient;

  @BeforeEach
  void doBeforeEach() {
    postgresClient = new PostgresClient(connectionFactory);
  }

  @Test
  void fetch_completes_forSqlQuery() {
    var query = "select 1 from table";

    Map<String, Object> rowData = Map.of("k", "v");

    var statement = mockStatement(rowData);

    var connection = mockPostgresConnection(statement, query);

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    StepVerifier.create(postgresClient.fetch(query))
        .expectNext(rowData)
        .verifyComplete();

    verify(connection).close();
  }

  @Test
  void fetch_throwsError_forException() {
    var query = "select 1 from table";

    Flux<Map<String, Object>> rowData = Flux.error(new SQLException("Something went wrong executing the query!"));

    var statement = mockStatement(rowData);

    var connection = mockPostgresConnection(statement, query);

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    StepVerifier.create(postgresClient.fetch(query))
        .verifyError();

    verify(connection).close();
  }

  @Test
  void fetch_completes_forCancelWithPostgresConnection() {
    var query = "select 1 from table";

    Flux<Map<String, Object>> rowData = Flux.just(Map.of("k", "v"));

    var statement = mockStatement(rowData);

    var connection = mockPostgresConnection(statement, query);
    when(connection.cancelRequest()).thenReturn(Mono.empty());

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    var actual = postgresClient.fetch(query);

    actual.subscribe(new CancelOnNextSubscriber());

    StepVerifier.create(actual)
        .expectNext(Map.of("k", "v"))
        .verifyComplete();

    verify(connection).cancelRequest();
    verify(connection, times(2)).close();
  }

  @Test
  void fetch_completes_forCancelWithWrappedConnection() {
    var query = "select 1 from table";

    Flux<Map<String, Object>> rowData = Flux.just(Map.of("k", "v"));

    var statement = mockStatement(rowData);

    var connection = mock(WrappedConnection.class);

    when(connection.close()).thenReturn(Mono.empty());
    when(connection.createStatement(query)).thenReturn(statement);

    var postgresConnection = mock(PostgresqlConnection.class);
    when(postgresConnection.cancelRequest()).thenReturn(Mono.empty());
    when(connection.unwrap()).thenReturn(postgresConnection);

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    var actual = postgresClient.fetch(query);

    actual.subscribe(new CancelOnNextSubscriber());

    StepVerifier.create(actual)
        .expectNext(Map.of("k", "v"))
        .verifyComplete();

    verify(postgresConnection).cancelRequest();
    verify(connection, times(2)).close();
  }

  @Test
  void fetch_throwsError_forCancelWithUnknownConnection() {
    var query = "select 1 from table";

    Flux<Map<String, Object>> rowData = Flux.just(Map.of("k", "v"));

    var statement = mockStatement(rowData);

    var connection = mock(Connection.class);

    when(connection.close()).thenReturn(Mono.empty());
    when(connection.createStatement(query)).thenReturn(statement);

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    var actual = postgresClient.fetch(query);

    actual.subscribe(new CancelOnNextSubscriber());

    StepVerifier.create(actual)
        .expectErrorMessage("R2DBC connection could not be unwrapped.");

    verify(connection, times(1)).close();
  }

  @Test
  void fetch_completes_forQueryWithRowMapper() {
    var selectQuery = DSL.select(DSL.asterisk())
        .from(DSL.table("table"))
        .where(DSL.field("identifier", String.class)
            .eq(DSL.val("id-1")))
        .getQuery();

    var query = mock(Query.class);

    Map<String, Object> rowData = Map.of("a", "value");
    Map<String, Object> mappedData = Map.of("a", "mapped-value");

    when(query.getSelectQuery()).thenReturn(selectQuery);

    RowMapper<Map<String, Object>> rowMapper = mock(RowMapper.class);
    when(rowMapper.apply(rowData)).thenReturn(mappedData);

    when(query.getRowMapper()).thenReturn(rowMapper);

    var statement = mockStatement(rowData);

    var connection = mockPostgresConnection(statement, "select * from table where identifier = $1");

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    StepVerifier.create(postgresClient.fetch(query))
        .expectNext(mappedData)
        .verifyComplete();

    verify(connection).close();
  }

  private PostgresqlConnection mockPostgresConnection(PostgresqlStatement statement, String expectedQueryStatement) {
    var connection = mock(PostgresqlConnection.class);

    when(connection.close()).thenReturn(Mono.empty());

    when(connection.createStatement(expectedQueryStatement)).thenReturn(statement);
    return connection;
  }

  private PostgresqlStatement mockStatement(Map<String, Object> data) {
    return mockStatement(Flux.just(data));
  }

  private PostgresqlStatement mockStatement(Flux<Map<String, Object>> data) {
    var statement = mock(PostgresqlStatement.class);

    var result = mock(PostgresqlResult.class);
    when(result.map(any(BiFunction.class))).thenReturn(data);

    var fluxResult = Flux.just(result);

    lenient().when(statement.execute())
        .thenReturn(fluxResult);

    lenient().when(statement.bind(any(String.class), any(Object.class)))
        .thenReturn(statement);

    return statement;
  }

  private static class CancelOnNextSubscriber extends BaseSubscriber<Map<String, Object>> {
    @Override
    protected void hookOnNext(Map<String, Object> value) {
      cancel();
    }
  }

  private interface WrappedConnection extends Connection, Wrapped<PostgresqlConnection> {
  }
}
