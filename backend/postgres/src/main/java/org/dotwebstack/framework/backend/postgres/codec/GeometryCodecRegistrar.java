package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.codec.CodecRegistry;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Component
public class GeometryCodecRegistrar implements CodecRegistrar {

  static final String TYPE_NAME_GEOMETRY = "geometry";

  private static final String GEO_OID_STMT =
      String.format("SELECT t.oid, t.typname FROM pg_type t WHERE t.typname = '%s'", TYPE_NAME_GEOMETRY);

  @Override
  public Publisher<Void> register(PostgresqlConnection connection, ByteBufAllocator allocator, CodecRegistry registry) {
    return connection.createStatement(GEO_OID_STMT)
        .execute()
        .flatMap(result -> result.map((row, rowMetadata) -> row.get("oid", Integer.class)))
        .single()
        .doOnNext(oid -> registry.addFirst(new GeometryCodec(allocator, oid)))
        .then();
  }
}
