package org.dotwebstack.framework.backend.postgres.codec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.postgresql.api.PostgresqlStatement;
import io.r2dbc.postgresql.codec.CodecRegistry;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GeometryCodecRegistrarTest {

  @Mock
  private PostgresqlConnection connection;

  @Mock
  private CodecRegistry registry;

  private final GeometryCodecRegistrar registrar = new GeometryCodecRegistrar();

  @Test
  void register_registerOids_withQuery() {
    PostgresqlResult result = mock(PostgresqlResult.class);
    var row = mock(Row.class);
    when(row.get("typname", String.class)).thenReturn("geometry");
    when(row.get("oid", Integer.class)).thenReturn(1234);

    lenient().when(result.map(any(BiFunction.class)))
        .thenReturn(Flux.just(row));

    PostgresqlStatement statement = mock(PostgresqlStatement.class);

    when(statement.execute()).thenReturn(Flux.just(result));

    when(connection.createStatement(ArgumentMatchers.anyString())).thenReturn(statement);

    Mono.from(registrar.register(connection, ByteBufAllocator.DEFAULT, registry))
        .block();

    verify(registry, times(1)).addFirst(any());
  }
}
