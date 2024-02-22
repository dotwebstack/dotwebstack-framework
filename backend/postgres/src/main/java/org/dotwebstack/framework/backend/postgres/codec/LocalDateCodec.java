package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.EncodedParameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.Assert;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;

public class LocalDateCodec implements Codec<LocalDate> {

  private final Set<Integer> dataTypes;

  public LocalDateCodec(Set<Integer> dataTypes) {
    this.dataTypes = dataTypes;
  }


  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    return dataTypes.contains(dataType);
  }

  @Override
  public boolean canEncode(Object value) {
    return false;
  }

  @Override
  public boolean canEncodeNull(Class<?> type) {
    return false;
  }

  @Override
  public LocalDate decode(ByteBuf buffer, int dataType, Format format, Class<? extends LocalDate> type) {
    Assert.requireNonNull(buffer, "byteBuf must not be null");

    String dateString = "";

    if (format == FORMAT_TEXT) {
      dateString = ByteBufUtils.decode(buffer);
    }

    if (format == FORMAT_BINARY) {
      dateString = buffer.toString(StandardCharsets.UTF_8);
    }

    String yearSubstring;

    if (dateString.startsWith("-")) {
      yearSubstring = dateString.substring(0, dateString.indexOf("-", dateString.indexOf("-") + 1));
    } else {
      yearSubstring = dateString.substring(0, dateString.indexOf("-"));
    }

    if (yearSubstring.length() > 4 && !yearSubstring.startsWith("-")) {
      dateString = "+".concat(dateString);
    }

    return LocalDate.parse(dateString);
  }

  @Override
  public EncodedParameter encode(Object value) {
    return null;
  }

  @Override
  public EncodedParameter encode(Object value, int dataType) {
    return null;
  }

  @Override
  public EncodedParameter encodeNull() {
    return null;
  }
}
