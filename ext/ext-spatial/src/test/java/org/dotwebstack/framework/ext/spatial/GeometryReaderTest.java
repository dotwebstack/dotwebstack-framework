package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID_RD;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID_RDNAP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;
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
    Map<String, String> data = Map.of(FROM_WKT, wkt);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
    assertThat(geometry.getSRID(), is(SRID_RD));
  }

  @Test
  void readGeometry_for3dWkt_returns3dGeometry() {
    var wkt = "POINT (194936.73 470973.96 0.0)";
    Map<String, String> data = Map.of(FROM_WKT, wkt);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(geometry.getCoordinate()
        .getZ(), is(0.0));
    assertThat(geometry.getSRID(), is(SRID_RDNAP));
  }

  @Test
  void readGeometry_forWkb_returnsGeometry() {
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    Map<String, String> data = Map.of(FROM_WKB, wkb);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
    assertThat(geometry.getSRID(), is(SRID_RD));
  }

  @Test
  void readGeometry_forGeoJson_returnsGeometry() {
    var geoJson = "{\n" + "  \"type\": \"Point\",\n" + "  \"coordinates\": [\n" + "    194936.73,\n" + "    470973.96\n"
        + "  ],\n" + "  \"crs\": {\n" + "    \"type\": \"name\",\n" + "    \"properties\": {\n"
        + "      \"name\": \"EPSG:28992\"\n" + "    }\n" + "  }\n" + "}";
    Map<String, String> data = Map.of(FROM_GEOJSON, geoJson);

    Geometry geometry = GeometryReader.readGeometry(data);

    assertThat(geometry, instanceOf(Point.class));
    assertThat(geometry.getCoordinate()
        .getX(), is(194936.73));
    assertThat(geometry.getCoordinate()
        .getY(), is(470973.96));
    assertThat(Double.isNaN(geometry.getCoordinate()
        .getZ()), is(true));
    assertThat(geometry.getSRID(), is(SRID_RD));
  }

  @Test
  void readGeometry_forIllegalWkt_returnsException() {
    var wkt = "POINT (194936.73 470973.96";
    Map<String, String> data = Map.of(FROM_WKT, wkt);

    assertThrows(IllegalArgumentException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forIllegalWkb_returnsException() {
    var wkb = "blablabla";
    Map<String, String> data = Map.of(FROM_WKB, wkb);

    assertThrows(IllegalArgumentException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forIllegalGeoJson_returnsException() {
    var geoJson = "blablabla";
    Map<String, String> data = Map.of(FROM_GEOJSON, geoJson);

    assertThrows(IllegalArgumentException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forMultipleFilters_returnsException() {
    var wkt = "POINT (194936.73 470973.96)";
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    Map<String, String> data = Map.of(FROM_WKT, wkt, FROM_WKB, wkb);

    assertThrows(IllegalArgumentException.class, () -> GeometryReader.readGeometry(data));
  }

  @Test
  void readGeometry_forNoFilter_returnsException() {
    Map<String, String> data = Collections.emptyMap();

    assertThrows(IllegalArgumentException.class, () -> GeometryReader.readGeometry(data));
  }
}
