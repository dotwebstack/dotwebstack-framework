package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.core.RequestValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeometryReaderTest {

  @Test
  void readGeometry_for2dWkt_returns2dGeometry() {
    var wkt = "POINT (194936.73 470973.96)";
    Map<String, Object> data = Map.of(FROM_WKT, wkt);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
  }

  @Test
  void readGeometry_for3dWkt_returns3dGeometry() {
    var wkt = "POINT (194936.73 470973.96 0.0)";
    Map<String, Object> data = Map.of(FROM_WKT, wkt);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(geometry.getCoordinate()
        .getZ(), is(0.0));
  }

  @Test
  void readGeometry_forWkb_returnsGeometry() {
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    Map<String, Object> data = Map.of(FROM_WKB, wkb);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
  }

  @Test
  void readGeometry_forGeoJson_returnsGeometry() {
    var geoJson = """
        {
          "type": "Point",
          "coordinates": [
            194936.73,
            470973.96
          ],
          "crs": {
            "type": "name",
            "properties": {
              "name": "EPSG:28992"
            }
          }
        }""";
    Map<String, Object> data = Map.of(FROM_GEOJSON, geoJson);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
  }

  @Test
  void getGeometryFromGeoJson_throwsException_whenCoordinatesAreNull() {
    var geoJson = """
        {
          "type": "Point",
          "crs": {
            "type": "name",
            "properties": {
              "name": "EPSG:28992"
            }
          }
        }""";

    Map<String, Object> data = Map.of(FROM_GEOJSON, geoJson);

    var exception = assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
    assertThat(exception.getMessage(), is("Coordinates can't be null!"));
  }

  @Test
  void readGeometry_forIllegalWkt_returnsException() {
    var wkt = "POINT (194936.73 470973.96";
    Map<String, Object> data = Map.of(FROM_WKT, wkt);

    assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forIllegalWkb_returnsException() {
    var wkb = "blablabla";
    Map<String, Object> data = Map.of(FROM_WKB, wkb);

    assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forIllegalGeoJson_returnsException() {
    var geoJson = "blablabla";
    Map<String, Object> data = Map.of(FROM_GEOJSON, geoJson);

    assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forMultipleFilters_returnsException() {
    var wkt = "POINT (194936.73 470973.96)";
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    Map<String, Object> data = Map.of(FROM_WKT, wkt, FROM_WKB, wkb);

    assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forNoFilter_returnsException() {
    Map<String, Object> data = Collections.emptyMap();

    assertThrows(RequestValidationException.class, () -> GeometryReader.readGeometry(data));
  }
}
