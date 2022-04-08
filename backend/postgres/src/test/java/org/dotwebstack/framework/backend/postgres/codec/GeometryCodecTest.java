package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.netty.buffer.ByteBufAllocator;
import io.r2dbc.postgresql.codec.PostgresTypeIdentifier;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.util.ByteBufUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;

class GeometryCodecTest {

  private GeometryFactory geometryFactory;

  private GeometryCodec geometryCodec;

  @BeforeEach
  void doBeforeEach() {
    geometryFactory = new GeometryFactory();
    geometryCodec = new GeometryCodec(1);
  }

  @Test
  void canDecode_returnsTrue_forSupportedDataType() {
    var actual = geometryCodec.canDecode(1, FORMAT_BINARY, Geometry.class);

    assertThat(actual, equalTo(true));
  }

  @Test
  void canDecode_returnsFalse_forUnsupportedDataType() {
    var actual = geometryCodec.canDecode(2, FORMAT_BINARY, Geometry.class);

    assertThat(actual, equalTo(false));
  }

  @Test
  void canEncode_returnsTrue_forSupportedObject() {
    var object = geometryFactory.createPoint();

    var actual = geometryCodec.canEncode(object);

    assertThat(actual, equalTo(true));
  }

  @Test
  void canEncode_returnsFalse_forUnsupportedObject() {
    var object = "foo";

    var actual = geometryCodec.canEncode(object);

    assertThat(actual, equalTo(false));
  }

  @Test
  void canEncodeNull_returnsTrue_forSupportedClass() {
    var actual = geometryCodec.canEncodeNull(Geometry.class);

    assertThat(actual, equalTo(true));
  }

  @Test
  void canEncodeNull_returnsFalse_forSupportedClass() {
    var actual = geometryCodec.canEncodeNull(String.class);

    assertThat(actual, equalTo(false));
  }

  @Test
  void encodeWithDatatype_returnsEncodedParameter_forGeometry() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var actual = geometryCodec.encode(geometry, 1);

    assertThat(actual, notNullValue());
  }

  @Test
  void encode_returnsEncodedParameter_forGeometry() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var actual = geometryCodec.encode(geometry);

    assertThat(actual, notNullValue());
  }

  @Test
  void decode_returnsGeometry_forByteBufTextFormatted() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var byteBuf = ByteBufUtils.encode(ByteBufAllocator.DEFAULT, WKBWriter.toHex(new WKBWriter(2).write(geometry)));

    var actual = geometryCodec.decode(byteBuf, 1, Format.FORMAT_TEXT, Geometry.class);

    assertThat(actual, equalTo(geometry));
  }

  @Test
  void decode_returnsGeometry_forByteBufBinaryFormatted() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var byteBuf = ByteBufUtils.encode(ByteBufAllocator.DEFAULT, WKBWriter.toHex(new WKBWriter(2).write(geometry)));

    var thrown = Assertions.assertThrows(IllegalArgumentException.class,
        () -> geometryCodec.decode(byteBuf, 1, FORMAT_BINARY, Geometry.class));
    assertThat(thrown.getMessage(), equalTo("format must be FORMAT_TEXT"));
  }

  @Test
  void decode_throwsException_forUnparsableByteBuf() {
    var byteBuf =
        ByteBufUtils.encode(ByteBufAllocator.DEFAULT, WKBWriter.toHex("foo".getBytes(StandardCharsets.UTF_8)));

    var thrown = Assertions.assertThrows(IllegalArgumentException.class,
        () -> geometryCodec.decode(byteBuf, 1, FORMAT_TEXT, Geometry.class));
    assertThat(thrown.getMessage(), equalTo("Unable to read WKB geometry"));
  }

  @Test
  void decode_returnsNull_forByteBufNull() {
    var actual = geometryCodec.decode(null, 1, Format.FORMAT_TEXT, Geometry.class);

    assertThat(actual, nullValue());
  }

  @Test
  void type_returnsGeometryClass() {
    assertThat(geometryCodec.type(), equalTo(Geometry.class));
  }

  @Test
  void getDatatypes_returnsIterable_forDefault() {
    var actual = StreamSupport.stream(geometryCodec.getDataTypes()
        .spliterator(), false)
        .map(PostgresTypeIdentifier::getObjectId)
        .collect(Collectors.toList());

    assertThat(actual, equalTo(List.of(1)));
  }

}
