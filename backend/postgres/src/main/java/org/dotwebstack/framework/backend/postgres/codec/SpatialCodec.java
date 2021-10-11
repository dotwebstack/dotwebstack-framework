package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static org.dotwebstack.framework.backend.postgres.codec.SpatialCodecRegistrar.TYPE_NAME_GEOMETRY;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.postgis.jts.JtsBinaryWriter;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
class SpatialCodec implements Codec<Geometry> {

  private final Map<String, Integer> dataTypes;

  private final ByteBufGeometryParser geometryParser;

  private final JtsBinaryWriter jtsBinaryWriter = new JtsBinaryWriter();

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    return dataTypes.containsValue(dataType);
  }

  @Override
  public Geometry decode(ByteBuf byteBuf, int dataType, Format format, Class<? extends Geometry> type) {
    if (byteBuf == null) {
      return null;
    }

    if (format.equals(Format.FORMAT_TEXT)) {
      var hex = byteBuf.toString(StandardCharsets.US_ASCII);
      return geometryParser.parse(hex);
    }

    return geometryParser.parse(byteBuf);
  }

  @Override
  public boolean canEncode(Object o) {
    return o instanceof Geometry;
  }

  @Override
  public boolean canEncodeNull(Class<?> type) {
    return type.isAssignableFrom(Geometry.class);
  }

  @Override
  public Parameter encode(Object o) {
    return new Parameter(FORMAT_BINARY, dataTypes.get(TYPE_NAME_GEOMETRY),
        Flux.just(Unpooled.wrappedBuffer(jtsBinaryWriter.writeBinary((Geometry) o))));
  }

  @Override
  public Parameter encodeNull() {
    return new Parameter(FORMAT_BINARY, dataTypes.get(TYPE_NAME_GEOMETRY), Parameter.NULL_VALUE);
  }

  @Override
  public Class<?> type() {
    return Geometry.class;
  }
}
