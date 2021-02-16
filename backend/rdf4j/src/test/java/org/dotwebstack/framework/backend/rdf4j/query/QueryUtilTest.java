package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.getLocalName;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.getPropertyPathLocalName;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.parseGeometryOrNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
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

  @Test
  void getPropertyPathLocalName_returnsNull_forNullPropertyShape() {
    // Arrange
    PropertyShape testValue = null;

    // Act
    String localName = getPropertyPathLocalName(testValue);

    // Assert
    assertThat(localName, is(nullValue()));
  }

  @Test
  void getPropertyPathLocalName_returnsNull_forNullPath() {
    // Arrange
    PropertyShape testValue = PropertyShape.builder()
        .path(null)
        .build();

    // Act
    String localName = getPropertyPathLocalName(testValue);

    // Assert
    assertThat(localName, is(nullValue()));
  }

  @Test
  void getPropertyPathLocalName_returnsName_default() {
    // Arrange
    String name = "monkey";
    IRI iri = mock(IRI.class);
    when(iri.getLocalName()).thenReturn(name);
    PredicatePath path = PredicatePath.builder()
        .iri(iri)
        .build();
    PropertyShape testValue = PropertyShape.builder()
        .path(path)
        .build();

    // Act
    String localName = getPropertyPathLocalName(testValue);

    // Assert
    assertThat(localName, is(notNullValue()));
    assertThat(localName, is(name));
  }

  @Test
  void getLocalName_returnsNull_forNullPath() {
    // Arrange
    BasePath testValue = null;

    // Act
    String localName = getLocalName(testValue);

    // Assert
    assertThat(localName, is(nullValue()));
  }

  @Test
  void getLocalName_returnsNull_forUnknownPathType() {
    // Arrange
    BasePath testValue = mock(BasePath.class);

    // Act
    String localName = getLocalName(testValue);

    // Assert
    assertThat(localName, is(nullValue()));
  }

  @Test
  void getLocalName_returnsNull_forNullIri() {
    // Arrange
    PredicatePath testValue = PredicatePath.builder()
        .iri(null)
        .build();

    // Act
    String localName = getLocalName(testValue);

    // Assert
    assertThat(localName, is(nullValue()));
  }

  @Test
  void getLocalName_returnsName_default() {
    // Arrange
    String name = "monkey";
    IRI iri = mock(IRI.class);
    when(iri.getLocalName()).thenReturn(name);
    PredicatePath testValue = PredicatePath.builder()
        .iri(iri)
        .build();

    // Act
    String localName = getLocalName(testValue);

    // Assert
    assertThat(localName, is(notNullValue()));
    assertThat(localName, is(name));
  }
}
