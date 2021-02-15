package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.parseGeometryOrNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class QueryUtilTest {


  @Test
  void parseGeometryOrNull_returnsNull_forNullValue() {
    // Arrange
    String testValue = null;

    // Act
    Geometry parsedValue = parseGeometryOrNull(testValue);

    // Assert
    assertThat(parsedValue, is(nullValue()));
  }

  @Test
  void parseGeometryOrNull_returnsGeometry_forCorrectInput() {
    // Arrange
    String testValue = "LINESTRING (0 0, 0 10)";

    // Act
    Geometry parsedValue = parseGeometryOrNull(testValue);

    // Assert
    assertThat(parsedValue, is(notNullValue()));
    assertThat(parsedValue.toString(), is(testValue));
  }

  @Test
  void parseGeometryOrNull_returnsNull_forInCorrectInput() {
    // Arrange
    String testValue = "monkey";

    // Act
    Geometry parsedValue = parseGeometryOrNull(testValue);

    // Assert
    assertThat(parsedValue, is(nullValue()));
  }
}
