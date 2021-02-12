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
    // Act
    boolean canDecode = codec.canDecode(id, Format.FORMAT_TEXT, Object.class);

    // Assert
    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canDecode_returnsFalse_forUnknownGeoTypes() {
    // Act
    boolean canDecode = codec.canDecode(2345, Format.FORMAT_TEXT, Object.class);

    // Assert
    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void decode_returnsGeometry_forHexString() {
    // Arrange

    String hexString = "0101000000332923E4C6EA1740C403B4D2CB1B4A40";
    ByteBuf byteBuf = Unpooled.wrappedBuffer(hexString.getBytes());

    // Act
    Geometry decodedValue = codec.decode(byteBuf, id, Format.FORMAT_TEXT, Geometry.class);

    // Assert
    assertThat(decodedValue, is(notNullValue()));
    assertThat(decodedValue.toString(), is(equalTo("POINT (5.979274334569982 52.21715768613606)")));
  }

  @Test
  void decode_returnsNull_forNull() {
    // Act
    Geometry decodedValue = codec.decode(null, id, Format.FORMAT_TEXT, Geometry.class);

    // Assert
    assertThat(decodedValue, is(nullValue()));
  }

  @Test
  void canEncode_returnsFalse_always() {
    // Act
    boolean canEncode = codec.canEncode(new Object());

    // Assert
    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void canEncodeNull_returnsFalse_always() {
    // Act
    boolean canEncode = codec.canEncodeNull(Object.class);

    // Assert
    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void encode_throwsException_always() {
    // Arrange
    Object encodedValue = new Object();

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () -> codec.encode(encodedValue));
  }

  @Test
  void encodeNull_throwsException_always() {
    // Act & Assert
    assertThrows(UnsupportedOperationException.class, codec::encodeNull);
  }

  @Test
  void type_returnsGeometry_always() {
    // Act
    Class<?> type = codec.type();

    // Assert
    assertThat(type, is(Geometry.class));
  }
}
