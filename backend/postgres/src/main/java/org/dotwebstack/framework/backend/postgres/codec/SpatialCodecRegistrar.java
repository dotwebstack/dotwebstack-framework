package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.codec.CodecRegistry;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Component
public class SpatialCodecRegistrar implements CodecRegistrar {

  private static final String GEO_OID_STMT =
      "SELECT t.oid FROM pg_type t WHERE t.typname = 'geography' OR t.typname = 'geometry'";

  @Override
  public Publisher<Void> register(PostgresqlConnection connection, ByteBufAllocator allocator, CodecRegistry registry) {
    return connection.createStatement(GEO_OID_STMT)
        .execute()
        .flatMap(result -> result.map((row, rowMetadata) -> row.get("oid", Integer.class)))
        .collect(Collectors.toSet())
        .doOnNext(dataTypes -> registry.addLast(new SpatialCodec(dataTypes)))
        .then();
  }
}
