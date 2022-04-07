package org.dotwebstack.framework.backend.postgres;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.postgresql.api.PostgresqlStatement;
import io.r2dbc.spi.ConnectionFactory;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PostgresClientTest {

  @Mock
  private ConnectionFactory connectionFactory;

  private PostgresClient postgresClient;

  @BeforeEach
  public void doBeforeEach() {
    postgresClient = new PostgresClient(connectionFactory);
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void fetch_returnsMap_forSqlQuery() {
    var sql = "select 1 from table";

    var connection = mock(PostgresqlConnection.class);
    when(connection.close()).thenReturn(Mono.empty());

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    var statement = mock(PostgresqlStatement.class);

    var result = mock(PostgresqlResult.class);

    Map<String, Object> data = Map.of("a", "value");

    when(result.map(any(BiFunction.class))).thenReturn(Flux.just(data));

    when(statement.execute()).thenReturn(Flux.just(result));

    when(connection.createStatement(sql)).thenReturn(statement);

    var actual = postgresClient.fetch(sql);

    StepVerifier.create(actual)
        .expectNext(data)
        .verifyComplete();
    verify(connection).close();
  }


  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void fetch_returnsMap_forQueryWithRowMapper() {
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

    var connection = mock(PostgresqlConnection.class);
    when(connection.close()).thenReturn(Mono.empty());

    when(connectionFactory.create()).thenReturn((Publisher) Mono.just(connection));

    var statement = mock(PostgresqlStatement.class);

    var result = mock(PostgresqlResult.class);

    when(result.map(any(BiFunction.class))).thenReturn(Flux.just(rowData));

    when(statement.execute()).thenReturn(Flux.just(result));
    when(statement.bind(any(String.class), any(Object.class))).thenReturn(statement);

    when(connection.createStatement("select * from table where identifier = $1")).thenReturn(statement);

    var actual = postgresClient.fetch(query);

    StepVerifier.create(actual)
        .expectNext(mappedData)
        .verifyComplete();
    verify(connection).close();
  }

}
