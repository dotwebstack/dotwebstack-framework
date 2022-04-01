package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.client.EncodedParameter.NULL_VALUE;
import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.client.EncodedParameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.codec.CodecMetadata;
import io.r2dbc.postgresql.codec.PostgresTypeIdentifier;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.Assert;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.util.Collections;
import javax.annotation.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import reactor.core.publisher.Mono;

class GeometryCodec implements Codec<Geometry>, CodecMetadata {

  private static final Class<Geometry> TYPE = Geometry.class;

  private final ByteBufAllocator byteBufAllocator;

  private final GeometryFactory geometryFactory = new GeometryFactory();

  private final int oid;

  GeometryCodec(ByteBufAllocator byteBufAllocator, int oid) {
    this.byteBufAllocator = Assert.requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");
    this.oid = oid;
  }

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    Assert.requireNonNull(format, "format must not be null");
    Assert.requireNonNull(type, "type must not be null");

    return dataType == oid;
  }

  @Override
  public boolean canEncode(Object value) {
    Assert.requireNonNull(value, "value must not be null");

    return TYPE.isInstance(value);
  }

  @Override
  public boolean canEncodeNull(Class<?> type) {
    Assert.requireNonNull(type, "type must not be null");

    return TYPE.isAssignableFrom(type);
  }

  @Override
  public Geometry decode(@Nullable ByteBuf buffer, int dataType, Format format, Class<? extends Geometry> type) {
    if (buffer == null) {
      return null;
    }

    Assert.isTrue(format == FORMAT_TEXT, "format must be FORMAT_TEXT");

    try {
      return new WKBReader(geometryFactory).read(WKBReader.hexToBytes(ByteBufUtils.decode(buffer)));
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public EncodedParameter encode(Object value) {
    Assert.requireType(value, Geometry.class, "value must be Geometry type");
    Geometry geometry = (Geometry) value;

    return new EncodedParameter(FORMAT_TEXT, oid,
        Mono.fromSupplier(() -> ByteBufUtils.encode(byteBufAllocator, geometry.toText())));
  }

  @Override
  public EncodedParameter encode(Object value, int dataType) {
    return encode(value);
  }

  @Override
  public EncodedParameter encodeNull() {
    return new EncodedParameter(FORMAT_BINARY, oid, NULL_VALUE);
  }

  @Override
  public Class<?> type() {
    return TYPE;
  }

  @Override
  public Iterable<PostgresTypeIdentifier> getDataTypes() {
    return Collections.singleton(() -> oid);
  }
}
