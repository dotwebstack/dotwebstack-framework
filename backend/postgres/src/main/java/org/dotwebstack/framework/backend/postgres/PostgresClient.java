package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.dotwebstack.framework.backend.postgres.query.Query;
import org.jooq.Param;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.conf.ParamType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PostgresClient {

  private final ConnectionFactory connectionFactory;

  public PostgresClient(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public Flux<Map<String, Object>> fetch(String sql) {
    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> {
          var statement = connection.createStatement(sql);

          return Mono.from(statement.execute())
              .flatMapMany(result -> result.map(PostgresClient::rowToMap))
              .doFinally(signalType -> Mono.from(connection.close())
                  .subscribe());
        });
  }

  public Flux<Map<String, Object>> fetch(Query query) {
    return Mono.from(connectionFactory.create())
        .flatMapMany(connection -> {
          var statement = createStatement(connection, query.getSelectQuery());

          return Mono.from(statement.execute())
              .flatMapMany(result -> result.map(PostgresClient::rowToMap))
              .map(query.getRowMapper())
              .doFinally(signalType -> Mono.from(connection.close())
                  .subscribe());
        });
  }

  private static Statement createStatement(Connection connection, SelectQuery<Record> query) {
    var sql = query.getSQL(ParamType.NAMED)
        .replace(":", "$");

    var params = query.getParams()
        .values()
        .stream()
        .filter(Predicate.not(Param::isInline))
        .collect(Collectors.toList());

    var statement = connection.createStatement(sql);

    for (var index = 0; index < params.size(); index++) {
      var paramBinding = "$".concat(String.valueOf(index + 1));
      var paramValue = Objects.requireNonNull(params.get(index)
          .getValue());
      statement = statement.bind(paramBinding, paramValue);
    }

    return statement;
  }

  private static Map<String, Object> rowToMap(Row row, RowMetadata rowMetadata) {
    var columnMetadatas = rowMetadata.getColumnMetadatas();
    var mapOfColValues = new LinkedCaseInsensitiveMap<>(columnMetadatas.size());

    IntStream.range(0, columnMetadatas.size())
        .forEach(index -> {
          var columnMetadata = columnMetadatas.get(index);
          mapOfColValues.put(columnMetadata.getName(), row.get(index));
        });

    return mapOfColValues;
  }
}
