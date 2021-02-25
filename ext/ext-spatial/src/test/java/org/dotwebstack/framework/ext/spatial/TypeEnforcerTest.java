package org.dotwebstack.framework.ext.spatial;


import static org.dotwebstack.framework.ext.spatial.GeometryType.LINESTRING;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTILINESTRING;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTIPOINT;
import static org.dotwebstack.framework.ext.spatial.GeometryType.MULTIPOLYGON;
import static org.dotwebstack.framework.ext.spatial.GeometryType.POINT;
import static org.dotwebstack.framework.ext.spatial.GeometryType.POLYGON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class TypeEnforcerTest {

  private final String geometryLineString = "LINESTRING(14.593449442863683 49.77692837399132,18.218937724113683 "
      + "50.11627285262387,17.977238505363683 49.34938190026593,14.593449442863683 49.77692837399132)";

  private final String geometryPointString = "POINT (15.892 48.483)";

  private final String geometryPolygonString = "POLYGON((14.769230692863683 48.7843473869024,17.032414286613683 "
      + "48.84945463293702,17.010441630363683 48.19457338438695,14.791203349113683 "
      + "48.09927916119722,14.769230692863683 48.7843473869024))";

  private final String geometryMultilineString = "MULTILINESTRING ((14.593449442863683 49.77692837399132, "
      + "18.218937724113683 50.11627285262387, 17.977238505363683 49.34938190026593, "
      + "14.593449442863683 49.77692837399132))";

  private final String geometryMultipointString = "MULTIPOINT ((15.892 48.483))";

  private final String geometryMultipolygonString = "MULTIPOLYGON (((14.769230692863683 48.7843473869024, "
      + "17.032414286613683 48.84945463293702, 17.010441630363683 48.19457338438695, "
      + "14.791203349113683 48.09927916119722, 14.769230692863683 48.7843473869024)))";

  private Geometry line;

  private Geometry point;

  private Geometry polygon;

  private Geometry multiline;

  private Geometry multipoint;

  private Geometry multipolygon;

  private TypeEnforcer typeEnforcer;

  @BeforeEach
  void setup() throws ParseException {
    typeEnforcer = new TypeEnforcer();

    WKTReader reader = new WKTReader();
    line = reader.read(geometryLineString);
    point = reader.read(geometryPointString);
    polygon = reader.read(geometryPolygonString);
    multiline = reader.read(geometryMultilineString);
    multipoint = reader.read(geometryMultipointString);
    multipolygon = reader.read(geometryMultipolygonString);
  }

  @Test
  void enforce_returnsCorrectScale_forFloatPrecision() throws ParseException {
    String geometryString = "LINESTRING(14.593449442863683 49.77692837399132,18.218937724113683 "
        + "50.11627285262387,17.977238505363683 49.34938190026593,14.593449442863683 49.77692837399132)";
    String expectedResult = "POINT (16.527 49.758)";

    WKTReader reader = new WKTReader();
    Geometry value = reader.read(geometryString);

    Geometry result = typeEnforcer.enforce(POINT, value);

    assertThat(result.toString(), is(expectedResult));
  }

  @Test
  void enforce_returnsCorrectScale_forFixedPrecision() throws ParseException {
    String geometryString = "LINESTRING(14.593449442863683 49.77692837399132,18.218937724113683 "
        + "50.11627285262387,17.977238505363683 49.34938190026593,14.593449442863683 49.77692837399132)";
    String expectedResult = "POINT (16.527 49.758)";

    PrecisionModel precisionModel = new PrecisionModel(10000);
    GeometryFactory factory = new GeometryFactory(precisionModel);
    WKTReader reader = new WKTReader(factory);
    Geometry value = reader.read(geometryString);

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
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultipoint() {
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOINT, line));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOINT, multiline));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOINT, multipolygon));
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultiline() {
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTILINESTRING, point));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTILINESTRING, polygon));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTILINESTRING, multipoint));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTILINESTRING, multipolygon));
  }

  @Test
  void enforce_throwsException_forUnsupportedConversionMultipolygon() {
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, point));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, multiline));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(MULTIPOLYGON, multipoint));
  }

  @Test
  void enforce_throwsException_forNull() {
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(LINESTRING, point));
    assertThrows(IllegalArgumentException.class, () -> typeEnforcer.enforce(POLYGON, point));
  }
}
