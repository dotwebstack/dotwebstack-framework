package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
    String[] enumArrayValues = new String[] {"foo", "bar"};

    String[] decodedValue =
        codec.decode(createEnumArrayBuffer(enumArrayValues), 1234, Format.FORMAT_BINARY, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_throwsException_ForNull() {
    assertThrows(IllegalArgumentException.class, () -> codec.decode(null, 1234, Format.FORMAT_BINARY, String[].class));
  }

  @Test
  void decode_throwsException_ForFormatText() {
    String[] enumArrayValues = new String[] {"test"};

    ByteBuf byteBuf = createEnumArrayBuffer(enumArrayValues);

    assertThrows(UnsupportedOperationException.class,
        () -> codec.decode(byteBuf, 1234, Format.FORMAT_TEXT, String[].class));
  }

  @Test
  void decode_returnsEmptyStringArray_ForNotReadableBuffer() {
    String[] decodesValues = codec.decode(Unpooled.buffer(), 1234, Format.FORMAT_BINARY, String[].class);

    assertThat(decodesValues, equalTo(new String[] {}));
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

  private ByteBuf createEnumArrayBuffer(String... values) {
    ByteBuf byteBuf = Unpooled.buffer();

    byteBuf.writeBytes(new byte[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -9, -60, 0, 0, 0});
    byteBuf.writeByte(values.length);
    byteBuf.writeBytes(new byte[] {0, 0, 0, 1});

    List.of(values)
        .forEach(value -> {
          byteBuf.writeBytes(new byte[] {0, 0, 0});
          byteBuf.writeByte(value.length());
          byteBuf.writeBytes(value.getBytes(StandardCharsets.UTF_8));
        });
    return byteBuf;
  }
}
