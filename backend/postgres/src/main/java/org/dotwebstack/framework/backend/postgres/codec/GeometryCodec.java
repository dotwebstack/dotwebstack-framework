package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.client.EncodedParameter.NULL_VALUE;
import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.client.EncodedParameter;
import io.r2dbc.postgresql.codec.Codec;
import io.r2dbc.postgresql.codec.CodecMetadata;
import io.r2dbc.postgresql.codec.PostgresTypeIdentifier;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.Assert;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import reactor.core.publisher.Flux;

class GeometryCodec implements Codec<Geometry>, CodecMetadata {

  private static final Class<Geometry> TYPE = Geometry.class;

  private final GeometryFactory geometryFactory = new GeometryFactory();

  private final int oid;

  private final Map<String, Integer> objectIds;

  GeometryCodec(Map<String, Integer> objectIds) {
    this.objectIds = objectIds;
    // It does not matter for the Object type if it's a geometry or geography type.
    oid = objectIds.get("geometry");
  }

  @Override
  public boolean canDecode(int dataType, Format format, Class<?> type) {
    Assert.requireNonNull(format, "format must not be null");
    Assert.requireNonNull(type, "type must not be null");

    return objectIds.containsValue(dataType);
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
      throw new IllegalArgumentException("Unable to read WKB geometry", e);
    }
  }

  @Override
  public EncodedParameter encode(Object value) {
    return encode(value, oid);
  }

  @Override
  public EncodedParameter encode(Object value, int dataType) {
    Assert.requireType(value, Geometry.class, "value must be Geometry type");
    var geometry = (Geometry) value;

    Assert.requireNonNull(geometry.getCoordinate(), "Encoding of geometry failed, coordinate can't be empty.");

    var outputDimension = Double.isNaN(geometry.getCoordinate()
        .getZ()) ? 2 : 3;

    var wkbWriter = new WKBWriter(outputDimension, true);

    return new EncodedParameter(FORMAT_BINARY, dataType, Flux.just(Unpooled.wrappedBuffer(wkbWriter.write(geometry))));
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
