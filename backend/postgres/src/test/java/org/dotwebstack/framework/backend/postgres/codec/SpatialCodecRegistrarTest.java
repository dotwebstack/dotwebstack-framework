package org.dotwebstack.framework.backend.postgres.codec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.postgresql.api.PostgresqlStatement;
import io.r2dbc.postgresql.codec.CodecRegistry;
import java.util.AbstractMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class SpatialCodecRegistrarTest {

  @Mock
  private ByteBufGeometryParser geometryParser;

  @Mock
  private PostgresqlConnection connection;

  @Mock
  private ByteBufAllocator allocator;

  @Mock
  private CodecRegistry registry;

  private final SpatialCodecRegistrar codecRegistrar = new SpatialCodecRegistrar(geometryParser);

  @Test
  void register_shouldAdd_SpatialCodec() {
    AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<>("typname", "aa");

    PostgresqlResult result = mock(PostgresqlResult.class);
    Mockito.lenient()
        .when(result.map(any()))
        .thenReturn(Flux.just(entry));

    PostgresqlStatement statement = mock(PostgresqlStatement.class);
    when(statement.execute()).thenReturn(Flux.just(result));
    when(connection.createStatement(anyString())).thenReturn(statement);

    codecRegistrar.register(connection, allocator, registry);
    Mono.from(codecRegistrar.register(connection, allocator, registry))
        .block();

    verify(registry).addFirst(any(SpatialCodec.class));
  }
}
