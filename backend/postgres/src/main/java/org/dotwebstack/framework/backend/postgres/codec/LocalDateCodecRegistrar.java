package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.codec.CodecRegistry;
import io.r2dbc.postgresql.extension.CodecRegistrar;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Component
public class LocalDateCodecRegistrar implements CodecRegistrar {

  private static final String DATE_OID_STMT =
      "SELECT t.oid FROM pg_type t where t.typname = 'date' AND t.typcategory = 'D';";

  @Override
  public Publisher<Void> register(PostgresqlConnection connection, ByteBufAllocator allocator, CodecRegistry registry) {
    return connection.createStatement(DATE_OID_STMT)
        .execute()
        .flatMap(result -> result.map((row, rowMetadata) -> row.get("oid", Integer.class)))
        .collect(Collectors.toSet())
        .doOnNext(dataTypes -> registry.addFirst(new LocalDateCodec(dataTypes)))
        .then();
  }
}
