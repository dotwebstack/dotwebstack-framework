package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.postgis.jts.JtsBinaryParser;

@Slf4j
class SpatialCodec implements Codec<Geometry> {

  private final Set<Integer> dataTypes;

  private final JtsBinaryParser jtsBinaryParser;

  public SpatialCodec(Set<Integer> dataTypes) {
    this.dataTypes = dataTypes;
    this.jtsBinaryParser = new JtsBinaryParser();
  }

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    return dataTypes.contains(dataType);
  }

  @Override
  public Geometry decode(ByteBuf byteBuf, int dataType, Format format, Class<? extends Geometry> type) {
    if (byteBuf == null) {
      return null;
    }

    return jtsBinaryParser.parse(byteBuf.toString(StandardCharsets.UTF_8));
  }

  @Override
  public boolean canEncode(Object o) {
    return false;
  }

  @Override
  public boolean canEncodeNull(Class<?> type) {
    return false;
  }

  @Override
  public Parameter encode(Object o) {
    throw ExceptionHelper.unsupportedOperationException("unable to encode Geometry");
  }

  @Override
  public Parameter encodeNull() {
    throw ExceptionHelper.unsupportedOperationException("unable to encode Null");
  }

  @Override
  public Class<?> type() {
    return Geometry.class;
  }
}
