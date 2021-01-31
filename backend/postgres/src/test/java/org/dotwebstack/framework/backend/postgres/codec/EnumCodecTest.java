package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.message.Format;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnumCodecTest {

  private final EnumCodec codec = new EnumCodec(Set.of(1234));

  @Test
  void canDecode_ReturnsTrue_ForKnownEnumTypes() {
    // Act
    boolean canDecode = codec.canDecode(1234, Format.FORMAT_TEXT, Object.class);

    // Assert
    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canDecode_ReturnsTrue_ForUnknownEnumTypes() {
    // Act
    boolean canDecode = codec.canDecode(2345, Format.FORMAT_TEXT, Object.class);

    // Assert
    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void decode_ReturnsStringValue_ForEnumObject() {
    // Arrange
    String enumValue = "active";
    ByteBuf byteBuf = Unpooled.wrappedBuffer(enumValue.getBytes());

    // Act
    String decodedValue = codec.decode(byteBuf, 1234, Format.FORMAT_TEXT, String.class);

    // Assert
    assertThat(decodedValue, is(equalTo(enumValue)));
  }

  @Test
  void decode_ReturnsNull_ForNull() {
    // Act
    String decodedValue = codec.decode(null, 1234, Format.FORMAT_TEXT, String.class);

    // Assert
    assertThat(decodedValue, is(nullValue()));
  }

  @Test
  void canEncode_ReturnsFalse_Always() {
    // Act
    boolean canEncode = codec.canEncode(new Object());

    // Assert
    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void canEncodeNull_ReturnsFalse_Always() {
    // Act
    boolean canEncode = codec.canEncodeNull(Object.class);

    // Assert
    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void encode_ThrowsException_Always() {
    // Arrange
    Object encodedValue = new Object();

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () -> {
      codec.encode(encodedValue);
    });
  }

  @Test
  void encodeNull_ThrowsException_Always() {
    // Act & Assert
    assertThrows(UnsupportedOperationException.class, codec::encodeNull);
  }

  @Test
  void type_ReturnsString_Always() {
    // Act
    Class<?> type = codec.type();

    // Assert
    assertThat(type, is(String.class));
  }
}
