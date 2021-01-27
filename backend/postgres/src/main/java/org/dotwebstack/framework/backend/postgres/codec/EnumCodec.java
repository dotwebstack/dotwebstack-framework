package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.util.Set;

class EnumCodec implements Codec<String> {

  private final Set<Integer> dataTypes;

  public EnumCodec(Set<Integer> dataTypes) {
    this.dataTypes = dataTypes;
  }

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    return dataTypes.contains(dataType);
  }

  @Override
  public String decode(ByteBuf byteBuf, int dataType, Format format, Class<? extends String> type) {
    if (byteBuf == null) {
      return null;
    }

    return byteBuf.toString(StandardCharsets.UTF_8);
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Parameter encodeNull() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> type() {
    return String.class;
  }
}
