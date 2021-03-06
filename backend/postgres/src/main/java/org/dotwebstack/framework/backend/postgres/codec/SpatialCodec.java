package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.locationtech.jts.geom.Geometry;

@Slf4j
class SpatialCodec implements Codec<Geometry> {

  private final Set<Integer> dataTypes;

  private final ByteBufGeometryParser geometryParser;

  public SpatialCodec(Set<Integer> dataTypes, ByteBufGeometryParser geometryParser) {
    this.dataTypes = dataTypes;
    this.geometryParser = geometryParser;
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

    if (format.equals(Format.FORMAT_TEXT)) {
      var hex = byteBuf.toString(StandardCharsets.US_ASCII);
      return geometryParser.parse(hex);
    }

    return geometryParser.parse(byteBuf);
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
