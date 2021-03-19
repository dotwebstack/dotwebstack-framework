package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnumArrayCodecTest {
  private final EnumArrayCodec codec = new EnumArrayCodec(Set.of(1234));

  @Test
  void canDecode_ReturnsTrue_ForKnownEnumArrayTypes() {
    boolean canDecode = codec.canDecode(1234, Format.FORMAT_BINARY, String[].class);

    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canDecode_ReturnsTrue_ForUnknownEnumArrayTypes() {
    boolean canDecode = codec.canDecode(2345, Format.FORMAT_BINARY, String[].class);

    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void decode_ReturnsStringArray_ForEnumArrayObject() {
    String enumArrayValue = "test";

    String[] decodedValue =
        codec.decode(createEnumArrayBuffer(enumArrayValue), 1234, Format.FORMAT_BINARY, String[].class);

    assertThat(decodedValue, is(equalTo(new String[] {enumArrayValue})));
  }

  @Test
  void decode_throwsException_ForNull() {
    assertThrows(IllegalArgumentException.class, () -> codec.decode(null, 1234, Format.FORMAT_BINARY, String[].class));
  }

  @Test
  void canEncode_ReturnsFalse_Always() {
    boolean canEncode = codec.canEncode(new Object());

    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void canEncodeNull_ReturnsFalse_Always() {
    boolean canEncode = codec.canEncodeNull(Object.class);

    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void encode_ThrowsException_Always() {
    Object encodedValue = new Object();

    assertThrows(UnsupportedOperationException.class, () -> {
      codec.encode(encodedValue);
    });
  }

  @Test
  void encodeNull_ThrowsException_Always() {
    assertThrows(UnsupportedOperationException.class, codec::encodeNull);
  }

  @Test
  void type_ReturnsString_Always() {
    Class<?> type = codec.type();

    assertThat(type, is(String[].class));
  }

  private ByteBuf createEnumArrayBuffer(String value) {
    ByteBuf byteBuf = Unpooled.buffer();
    byteBuf.writeBytes(new byte[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -56, 42, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
    byteBuf.writeByte(value.length());
    byteBuf.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    return byteBuf;
  }
}
