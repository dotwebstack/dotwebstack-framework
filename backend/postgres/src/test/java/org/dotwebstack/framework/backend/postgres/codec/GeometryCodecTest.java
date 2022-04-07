package org.dotwebstack.framework.backend.postgres.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

class GeometryCodecTest {

  private GeometryFactory geometryFactory;

  private GeometryCodec geometryCodec;

  @BeforeEach
  public void doBeforeEach() {
    geometryFactory = new GeometryFactory();
    geometryCodec = new GeometryCodec(Map.of("geometry", 1));
  }

  @Test
  public void canDecode_returnsTrue_forSupportedDataType() {
    var actual = geometryCodec.canDecode(1, FORMAT_BINARY, Geometry.class);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void canDecode_returnsFalse_forUnsupportedDataType() {
    var actual = geometryCodec.canDecode(2, FORMAT_BINARY, Geometry.class);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void canEncode_returnsTrue_forSupportedObject() {
    var object = geometryFactory.createPoint();

    var actual = geometryCodec.canEncode(object);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void canEncode_returnsFalse_forUnsupportedObject() {
    var object = "foo";

    var actual = geometryCodec.canEncode(object);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void canEncodeNull_returnsTrue_forSupportedClass() {
    var actual = geometryCodec.canEncodeNull(Geometry.class);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void canEncodeNull_returnsFalse_forSupportedClass() {
    var actual = geometryCodec.canEncodeNull(String.class);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void encodeWithDatatype_returnsEncodedParameter_forGeometry() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var actual = geometryCodec.encode(geometry, 1);

    assertThat(actual, notNullValue());
  }

  @Test
  public void encode_returnsEncodedParameter_forGeometry() {
    var geometry = geometryFactory.createPoint(new Coordinate(1, 2));

    var actual = geometryCodec.encode(geometry);

    assertThat(actual, notNullValue());
  }

  @Test
  public void type_returnsGeometryClass() {
    assertThat(geometryCodec.type(), equalTo(Geometry.class));
  }

}
