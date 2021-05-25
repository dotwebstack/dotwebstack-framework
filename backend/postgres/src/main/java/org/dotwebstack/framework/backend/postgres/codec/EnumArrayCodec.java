package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.util.Assert.requireArrayDimension;
import static io.r2dbc.postgresql.util.Assert.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.Assert;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * EnumArray R2DBC codec to represent an enum array as string array.
 *
 * <p>
 * this implementation is based on {@link io.r2dbc.postgresql.codec.AbstractArrayCodec}
 * </p>
 */
public class EnumArrayCodec implements Codec<String[]> {

  private final Set<Integer> dataTypes;

  public static final char ARRAY_DELIM = ',';

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
      return decodeText(buffer, type);
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

    var dimensions = buffer.readInt();
    if (dimensions == 0) {
      return (String[]) Array.newInstance(String.class, 0);
    }

    if (returnType != Object.class) {
      requireArrayDimension(returnType, dimensions, "Dimensions mismatch: %s expected, but %s returned from DB");
    }

    buffer.skipBytes(4); // flags: 0=no-nulls, 1=has-nulls
    buffer.skipBytes(4); // element oid

    var dims = new int[dimensions];
    for (var d = 0; d < dimensions; ++d) {
      dims[d] = buffer.readInt(); // dimension size
      buffer.skipBytes(4); // lower bound ignored
    }

    String[] array = (String[]) Array.newInstance(String.class, dims);

    readArrayAsBinary(buffer, array, dims, 0);

    return array;
  }

  private void readArrayAsBinary(ByteBuf buffer, Object[] array, int[] dims, int thisDimension) {
    if (thisDimension == dims.length - 1) {
      for (var i = 0; i < dims[thisDimension]; ++i) {
        var len = buffer.readInt();
        if (len == -1) {
          continue;

        }
        array[i] = ByteBufUtils.decode(buffer.readSlice(len));
      }
    } else {
      for (var i = 0; i < dims[thisDimension]; ++i) {
        readArrayAsBinary(buffer, (Object[]) array[i], dims, thisDimension + 1);
      }
    }
  }

  private Class<?> createArrayType(int dims) {
    var size = new int[dims];
    Arrays.fill(size, 1);
    return Array.newInstance(String.class, size)
        .getClass();
  }

  private static int getDimensions(List<?> list) {
    var dims = 1;

    Object inner = list.get(0);

    while (inner instanceof List) {
      inner = ((List<?>) inner).get(0);
      dims++;
    }

    return dims;
  }

  private static String[] toArray(List<?> list, Class<?> returnType) {
    List<String> result = new ArrayList<>(list.size());

    for (Object e : list) {
      Object o = (e instanceof List ? toArray((List<?>) e, returnType.getComponentType()) : e);
      result.add(o.toString());
    }

    return result.toArray((String[]) Array.newInstance(returnType, list.size()));
  }

  private List<Object> buildArrayList(ByteBuf buf) { // NOSONAR
    List<Object> arrayList = new ArrayList<>();

    StringBuilder buffer = null;
    var insideString = false;
    var wasInsideString = false; // needed for checking if NULL
    // value occurred
    List<List<Object>> dims = new ArrayList<>(); // array dimension arrays
    List<Object> curArray = arrayList; // currently processed array

    var chars = buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8);

    // Starting with 8.0 non-standard (beginning index
    // isn't 1) bounds the dimensions are returned in the
    // data formatted like so "[0:3]={0,1,2,3,4}".
    // Older versions simply do not return the bounds.
    //
    // Right now we ignore these bounds, but we could
    // consider allowing these index values to be used
    // even though the JDBC spec says 1 is the first
    // index. I'm not sure what a client would like
    // to see, so we just retain the old behavior.
    var startOffset = 0;

    if (chars.charAt(0) == '[') {
      while (chars.charAt(startOffset) != '=') {
        startOffset++;
      }
      startOffset++; // skip =
    }

    char currentChar;

    for (int i = startOffset; i < chars.length(); i++) {
      currentChar = chars.charAt(i);
      var appendChar = true;
      // escape character that we need to skip
      if (currentChar == '\\') {
        appendChar = false;
      } else if (!insideString && currentChar == '{') {
        // subarray start
        if (dims.isEmpty()) {
          dims.add(arrayList);
        }
        curArray = dims.get(dims.size() - 1);

        for (int t = i + 1; t < chars.length(); t++) {
          if (!Character.isWhitespace(chars.charAt(t)) && chars.charAt(t) != '{') {
            break;
          }
        }

        buffer = new StringBuilder();
        appendChar = false;
      } else if (currentChar == '"') {
        // quoted element
        insideString = !insideString;
        wasInsideString = true;
        appendChar = false;
      } else if (!insideString && Character.isWhitespace(currentChar)) {
        // white space
        appendChar = false;
      } else if ((!insideString && (currentChar == ARRAY_DELIM || currentChar == '}')) || i == chars.length() - 1) {
        // array end or element end
        // when character that is a part of array element
        if (currentChar != '"' && currentChar != '}' && currentChar != ARRAY_DELIM && buffer != null) {
          buffer.append(currentChar);
        }

        String b = buffer == null ? null : buffer.toString();

        // add element to current array
        if (b != null && (!b.isEmpty() || wasInsideString)) {
          curArray.add(!wasInsideString && b.equals("NULL") ? null : b);
        }

        wasInsideString = false;
        buffer = new StringBuilder();

        // when end of an array
        if (currentChar == '}') {
          dims.remove(dims.size() - 1);

          // when multi-dimension
          if (!dims.isEmpty()) {
            curArray = dims.get(dims.size() - 1);
          }

          buffer = null;
        }

        appendChar = false;
      }

      if (buffer != null && appendChar) {
        buffer.append(currentChar);
      }
    }

    return arrayList;
  }

  private String[] decodeText(ByteBuf buffer, Class<?> returnType) {
    List<?> elements = buildArrayList(buffer);

    if (elements.isEmpty()) {
      return (String[]) Array.newInstance(String.class, 0);
    }

    int dimensions = getDimensions(elements);

    // TODO: type changed from String.class to Object.class, we cannot explain why this is necessary
    if (returnType != Object.class) {
      Assert.requireArrayDimension(returnType, dimensions, "Dimensions mismatch: %s expected, but %s returned from DB");
    }

    return toArray(elements, createArrayType(dimensions).getComponentType());
  }
}
