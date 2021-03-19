package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.util.Assert.requireArrayDimension;
import static io.r2dbc.postgresql.util.Assert.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.lang.reflect.Array;
import java.util.Set;

public class EnumArrayCodec implements Codec<String[]> {

  private final Set<Integer> dataTypes;

  public EnumArrayCodec(Set<Integer> dataTypes) {
    this.dataTypes = dataTypes;
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
  public String[] decode(ByteBuf buffer, int dataType, Format format, Class<? extends String[]> type) {
    requireNonNull(buffer, "byteBuf must not be null");
    requireNonNull(format, "format must not be null");
    requireNonNull(type, "type must not be null");

    if (FORMAT_BINARY == format) {
      return decodeBinary(buffer, type);
    } else {
      throw new UnsupportedOperationException("Text format not supported!");
    }
  }

  @Override
  public Parameter encode(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Parameter encodeNull() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> type() {
    return String[].class;
  }

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    return dataTypes.contains(dataType);
  }

  private String[] decodeBinary(ByteBuf buffer, Class<?> returnType) {
    if (!buffer.isReadable()) {
      return new String[0];
    }

    int dimensions = buffer.readInt();
    if (dimensions == 0) {
      return (String[]) Array.newInstance(String.class, 0);
    }

    if (returnType != Object.class) {
      requireArrayDimension(returnType, dimensions, "Dimensions mismatch: %s expected, but %s returned from DB");
    }

    buffer.skipBytes(4); // flags: 0=no-nulls, 1=has-nulls
    buffer.skipBytes(4); // element oid

    int[] dims = new int[dimensions];
    for (int d = 0; d < dimensions; ++d) {
      dims[d] = buffer.readInt(); // dimension size
      buffer.skipBytes(4); // lower bound ignored
    }

    String[] array = (String[]) Array.newInstance(String.class, dims);

    readArrayAsBinary(buffer, array, dims, 0);

    return array;
  }

  private void readArrayAsBinary(ByteBuf buffer, Object[] array, int[] dims, int thisDimension) {
    if (thisDimension == dims.length - 1) {
      for (int i = 0; i < dims[thisDimension]; ++i) {
        int len = buffer.readInt();
        if (len == -1) {
          continue;

        }
        array[i] = ByteBufUtils.decode(buffer.readSlice(len));
      }
    } else {
      for (int i = 0; i < dims[thisDimension]; ++i) {
        readArrayAsBinary(buffer, (Object[]) array[i], dims, thisDimension + 1);
      }
    }
  }
}
