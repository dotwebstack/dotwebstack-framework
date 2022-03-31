package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.message.Format;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.postgis.jts.JtsBinaryWriter;

class SpatialCodecTest {

  private final int id = 17991;

  private final String name = "geometry";

  private final ByteBufGeometryParser geometryParser = new ByteBufGeometryParser();

  private final SpatialCodec codec = new SpatialCodec(Map.of(name, id), geometryParser);

  @Test
  void canDecode_returnsTrue_forKnownGeoTypes() {
    boolean canDecode = codec.canDecode(id, Format.FORMAT_TEXT, Object.class);

    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canDecode_returnsFalse_forUnknownGeoTypes() {
    boolean canDecode = codec.canDecode(2345, Format.FORMAT_TEXT, Object.class);

    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void decode_returnsGeometry_forTextFormat() {
    Point point = (new GeometryFactory()).createPoint(new Coordinate(5.97927433, 52.21715768));
    String hex = new JtsBinaryWriter().writeHexed(point);
    ByteBuf byteBuf = Unpooled.wrappedBuffer(hex.getBytes());

    Geometry decodedValue = codec.decode(byteBuf, id, Format.FORMAT_TEXT, Geometry.class);

    assertThat(decodedValue, is(notNullValue()));
    assertThat(decodedValue.toString(), is(equalTo("POINT (5.97927433 52.21715768)")));
  }

  @Test
  void decode_returnsGeometry_forBinaryFormat() {
    Point point = new GeometryFactory().createPoint(new Coordinate(5.97927433, 52.21715768));
    byte[] bytes = new JtsBinaryWriter().writeBinary(point);
    ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

    Geometry decodedValue = codec.decode(byteBuf, id, Format.FORMAT_BINARY, Geometry.class);

    assertThat(decodedValue, is(notNullValue()));
    assertThat(decodedValue.toString(), is(equalTo("POINT (5.97927433 52.21715768)")));
  }

  @Test
  void decode_returnsNull_forNull() {
    Geometry decodedValue = codec.decode(null, id, Format.FORMAT_TEXT, Geometry.class);

    assertThat(decodedValue, is(nullValue()));
  }

  @Test
  void canEncode_returnsTrue_forGeometry() {
    boolean canDecode = codec.canEncode(new GeometryFactory().createPoint());

    assertThat(canDecode, is(Boolean.TRUE));
  }

  @Test
  void canEncode_returnsFalse_forObject() {
    boolean canDecode = codec.canEncode(new Object());

    assertThat(canDecode, is(Boolean.FALSE));
  }

  @Test
  void canEncodeNull_returnsTrue_forObject() {
    boolean canEncode = codec.canEncodeNull(Object.class);

    assertThat(canEncode, is(Boolean.TRUE));
  }

  @Test
  void canEncodeNull_returnsFalse_forPrimitive() {
    boolean canEncode = codec.canEncodeNull(Integer.class);

    assertThat(canEncode, is(Boolean.FALSE));
  }

  @Test
  void encode_returnsParameter_forGeometry() {
    Point point = new GeometryFactory().createPoint(new Coordinate(5.97927433, 52.21715768));

    Parameter encodedValue = codec.encode(point);

    assertThat(encodedValue, is(notNullValue()));
  }

  @Test
  void encodeNull_returnsParameter_always() {
    Parameter result = codec.encodeNull();

    assertThat(result.getClass(), is(Parameter.class));
  }
}
