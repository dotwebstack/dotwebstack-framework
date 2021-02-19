package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.postgis.binary.ByteGetter;
import org.postgis.jts.JtsBinaryParser;
import org.springframework.stereotype.Component;

@Component
class ByteBufGeometryParser extends JtsBinaryParser {

  public Geometry parse(ByteBuf byteBuf) {
    ByteBufByteGetter byteGetter = new ByteBufByteGetter(byteBuf);
    return parseGeometry(valueGetterForEndian(byteGetter));
  }

  @RequiredArgsConstructor
  public static class ByteBufByteGetter extends ByteGetter {

    private final ByteBuf byteBuf;

    @Override
    public int get(int i) {
      return byteBuf.getByte(i) & 0xFF;
    }
  }
}
