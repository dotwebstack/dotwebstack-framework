package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.codec.CodecRegistry;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Component
public class GeometryCodecRegistrar implements CodecRegistrar {

  static final String TYPE_NAME_GEOMETRY = "geometry";

  static final String TYPE_NAME_GEOGRAPHY = "geography";

  private static final String GEO_OID_STMT =
      String.format("SELECT t.oid, t.typname FROM pg_type t WHERE t.typname in ('%s', '%s')", TYPE_NAME_GEOMETRY,
          TYPE_NAME_GEOGRAPHY);

  @Override
  public Publisher<Void> register(PostgresqlConnection connection, ByteBufAllocator allocator, CodecRegistry registry) {
    return connection.createStatement(GEO_OID_STMT)
        .execute()
        .flatMap(result -> result.map((row, rowMetadata) -> row))
        .collect(Collectors.toMap(row -> row.get("typname", String.class), row -> row.get("oid", Integer.class)))
        .doOnNext(objectIds -> registry.addFirst(new GeometryCodec(objectIds)))
        .then();
  }
}
