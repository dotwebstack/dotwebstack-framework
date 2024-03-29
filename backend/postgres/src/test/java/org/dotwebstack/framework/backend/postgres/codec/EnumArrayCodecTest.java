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
import java.util.concurrent.atomic.AtomicBoolean;
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
  void decode_ReturnsStringArray_ForBinaryEnumArrayObject() {
    String[] enumArrayValues = new String[] {"foo", "bar"};

    String[] decodedValue =
        codec.decode(createBinaryBuffer(enumArrayValues), 1234, Format.FORMAT_BINARY, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_ReturnsStringArray_ForTextEnumArrayObject() {
    String[] enumArrayValues = new String[] {"foo", "bar"};

    String[] decodedValue = codec.decode(createTextBuffer(enumArrayValues), 1234, Format.FORMAT_TEXT, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_ReturnsStringArray_ForTextEnumArrayWithBoundsObject() {
    String[] enumArrayValues = new String[] {"foo", "bar"};

    String[] decodedValue =
        codec.decode(createTextBufferWithBounds(enumArrayValues), 1234, Format.FORMAT_TEXT, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_ReturnsStringArray_ForTextEnumQuotedArrayWithObject() {
    String[] enumArrayValues = new String[] {"foo", "bar"};

    String[] decodedValue =
        codec.decode(createTextBufferWithBounds("foo", "\"bar\""), 1234, Format.FORMAT_TEXT, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_ReturnsStringArray_ForTextEnumEmptyArray() {
    String[] enumArrayValues = new String[] {};

    String[] decodedValue = codec.decode(createTextBuffer(enumArrayValues), 1234, Format.FORMAT_TEXT, String[].class);

    assertThat(decodedValue, is(equalTo(enumArrayValues)));
  }

  @Test
  void decode_throwsException_ForNull() {
    assertThrows(IllegalArgumentException.class, () -> codec.decode(null, 1234, Format.FORMAT_BINARY, String[].class));
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

    assertThrows(UnsupportedOperationException.class, () -> codec.encode(encodedValue));
  }

  @Test
  void encodeNull_ThrowsException_Always() {
    assertThrows(UnsupportedOperationException.class, codec::encodeNull);
  }

  private ByteBuf createBinaryBuffer(String... values) {
    ByteBuf buffer = Unpooled.buffer();

    buffer.writeBytes(new byte[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -9, -60, 0, 0, 0});
    buffer.writeByte(values.length);
    buffer.writeBytes(new byte[] {0, 0, 0, 1});

    List.of(values)
        .forEach(value -> {
          buffer.writeBytes(new byte[] {0, 0, 0});
          buffer.writeByte(value.length());
          buffer.writeBytes(value.getBytes(StandardCharsets.UTF_8));
        });
    return buffer;
  }

  private ByteBuf createTextBufferWithBounds(String... values) {
    ByteBuf buffer = Unpooled.buffer();

    buffer.writeBytes(String.format("[%d:%d]=", 0, values.length)
        .getBytes(StandardCharsets.UTF_8));

    return createTextBuffer(buffer, values);
  }

  private ByteBuf createTextBuffer(String... values) {
    return createTextBuffer(Unpooled.buffer(), values);
  }

  private ByteBuf createTextBuffer(ByteBuf buffer, String... values) {
    AtomicBoolean first = new AtomicBoolean(true);

    buffer.writeBytes("{".getBytes(StandardCharsets.UTF_8));

    List.of(values)
        .forEach(value -> {
          if (!first.getAndSet(false)) {
            buffer.writeBytes(",".getBytes(StandardCharsets.UTF_8));
          }
          buffer.writeBytes(value.getBytes(StandardCharsets.UTF_8));

        });

    buffer.writeBytes("}".getBytes(StandardCharsets.UTF_8));

    return buffer;
  }
}
