package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.message.Format;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;


class SpatialCodecTest {

  private final int id = 17991;

  private final SpatialCodec codec = new SpatialCodec(Set.of(id));

  @Test
  void canDecode_returnsTrue_forKnownGeoTypes() {
    boolean canDecode = codec.canDecode(id, Format.FORMAT_TEXT, Object.class);

    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canDecode_returnsFalse_forUnknownGeoTypes() {
    boolean canDecode = codec.canDecode(2345, Format.FORMAT_TEXT, Object.class);

    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void decode_returnsGeometry_forHexString() {
    String hexString = "0101000000332923E4C6EA1740C403B4D2CB1B4A40";
    ByteBuf byteBuf = Unpooled.wrappedBuffer(hexString.getBytes());

    Geometry decodedValue = codec.decode(byteBuf, id, Format.FORMAT_TEXT, Geometry.class);

    assertThat(decodedValue, is(notNullValue()));
    assertThat(decodedValue.toString(), is(equalTo("POINT (5.979274334569982 52.21715768613606)")));
  }

  @Test
  void decode_returnsNull_forNull() {
    Geometry decodedValue = codec.decode(null, id, Format.FORMAT_TEXT, Geometry.class);

    assertThat(decodedValue, is(nullValue()));
  }

  @Test
  void canEncode_returnsFalse_always() {
    boolean canEncode = codec.canEncode(new Object());

    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void canEncodeNull_returnsFalse_always() {
    boolean canEncode = codec.canEncodeNull(Object.class);

    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void encode_throwsException_always() {
    Object encodedValue = new Object();

    assertThrows(UnsupportedOperationException.class, () -> codec.encode(encodedValue));
  }

  @Test
  void encodeNull_throwsException_always() {
    assertThrows(UnsupportedOperationException.class, codec::encodeNull);
  }

  @Test
  void type_returnsGeometry_always() {
    Class<?> type = codec.type();

    assertThat(type, is(Geometry.class));
  }
}
