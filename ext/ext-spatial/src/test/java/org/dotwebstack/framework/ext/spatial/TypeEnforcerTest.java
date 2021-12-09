package org.dotwebstack.framework.ext.spatial;


import static org.dotwebstack.framework.ext.spatial.GeometryType.GEOMETRYCOLLECTION;
import static org.dotwebstack.framework.ext.spatial.GeometryType.LINESTRING;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTILINESTRING;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTIPOINT;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTIPOLYGON;
import static org.dotwebstack.framework.ext.spatial.GeometryType.POINT;
import static org.dotwebstack.framework.ext.spatial.GeometryType.POLYGON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.testhelper.TestSpatialReferenceSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class TypeEnforcerTest {

  private final String geometryLineString = "LINESTRING(14.593449442863683 49.77692837399132,18.218937724113683 "
      + "50.11627285262387,17.977238505363683 49.34938190026593,14.593449442863683 49.77692837399132)";

  private final String geometryPointString = "POINT (15.891968943 48.482691551)";

  private final String geometryPolygonString = "POLYGON((14.769230692863683 48.7843473869024,17.032414286613683 "
      + "48.84945463293702,17.010441630363683 48.19457338438695,14.791203349113683 "
      + "48.09927916119722,14.769230692863683 48.7843473869024))";

  private final String geometryMultilineString = "MULTILINESTRING ((14.593449442863683 49.77692837399132, "
      + "18.218937724113683 50.11627285262387, 17.977238505363683 49.34938190026593, "
      + "14.593449442863683 49.77692837399132))";

  private final String geometryMultipointString = "MULTIPOINT ((15.891968943 48.482691551))";

  private final String geometryMultipolygonString = "MULTIPOLYGON (((14.769230692863683 48.7843473869024, "
      + "17.032414286613683 48.84945463293702, 17.010441630363683 48.19457338438695, "
      + "14.791203349113683 48.09927916119722, 14.769230692863683 48.7843473869024)))";

  private final String geometryGeometryCollectionString = "GEOMETRYCOLLECTION(POINT (15.892 48.483))";

  private Geometry line;

  private Geometry point;

  private Geometry polygon;

  private Geometry multiline;

  private Geometry multipoint;

  private Geometry multipolygon;

  private Geometry geometryCollection;

  private TypeEnforcer typeEnforcer;

  private Spatial spatial;

  @BeforeEach
  void setup() throws ParseException {
    Spatial spatial = new Spatial();
    spatial.setReferenceSystems(Map.of(28992, createSrs(4), 9067, createSrs(9)));

    typeEnforcer = new TypeEnforcer(spatial);

    WKTReader reader = new WKTReader();
    line = reader.read(geometryLineString);
    point = reader.read(geometryPointString);
    polygon = reader.read(geometryPolygonString);
    multiline = reader.read(geometryMultilineString);
    multipoint = reader.read(geometryMultipointString);
    multipolygon = reader.read(geometryMultipolygonString);
    geometryCollection = reader.read(geometryGeometryCollectionString);
  }

  private SpatialReferenceSystem createSrs(int scale) {
    TestSpatialReferenceSystem srs = new TestSpatialReferenceSystem();
    srs.setScale(scale);
    return srs;
  }

  @ParameterizedTest
  @CsvSource({"28992, POINT (16.5269 49.7582)", "9067, POINT (16.526893736 49.758249656)",
      "0, POINT (16.526893736 49.758249656)"})
  void enforce_returnsPoint_forLineString(Integer srid, String expectedResult) throws ParseException {
    WKTReader reader = new WKTReader();
    Geometry value = reader.read(geometryLineString);
    value.setSRID(srid);

    Geometry result = typeEnforcer.enforce(POINT, value);

    assertThat(result.toString(), is(expectedResult));
  }

  @Test
  void enforce_returnsPoint_forMultipolygon() {
    Geometry result = typeEnforcer.enforce(POINT, multipolygon);

    assertThat(result.toString(), is(geometryPointString));
  }

  @Test
  void enforce_returnsMultipoint_forPoint() {
    Geometry result = typeEnforcer.enforce(MULTIPOINT, point);

    assertThat(result.toString(), is(geometryMultipointString));
  }

  @Test
  void enforce_returnsMultiline_forLine() {
    Geometry result = typeEnforcer.enforce(MULTILINESTRING, line);

    assertThat(result.toString(), is(geometryMultilineString));
  }

  @Test
  void enforce_returnsMultipolygon_forPolygon() {
    Geometry result = typeEnforcer.enforce(MULTIPOLYGON, polygon);

    assertThat(result.toString(), is(geometryMultipolygonString));
  }

  @Test
  void enforce_returnsSameGeometry_forEqualType() {
    assertThat(line, is(typeEnforcer.enforce(LINESTRING, line)));
    assertThat(point, is(typeEnforcer.enforce(POINT, point)));
    assertThat(polygon, is(typeEnforcer.enforce(POLYGON, polygon)));
    assertThat(multiline, is(typeEnforcer.enforce(MULTILINESTRING, multiline)));
    assertThat(multipoint, is(typeEnforcer.enforce(MULTIPOINT, multipoint)));
    assertThat(multipolygon, is(typeEnforcer.enforce(MULTIPOLYGON, multipolygon)));
    assertThat(geometryCollection, is(typeEnforcer.enforce(GEOMETRYCOLLECTION, geometryCollection)));
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultipoint() {
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOINT, line));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOINT, multiline));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOINT, multipolygon));
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultiline() {
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTILINESTRING, point));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTILINESTRING, polygon));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTILINESTRING, multipoint));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTILINESTRING, multipolygon));
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultipolygon() {
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, point));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, multiline));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, multipoint));
  }

  @Test
  void enforce_throwsException_forUnsupportedTypes() {
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(LINESTRING, point));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(POLYGON, point));
    assertThrows(UnsupportedOperationException.class, () -> typeEnforcer.enforce(GEOMETRYCOLLECTION, point));
  }
}
