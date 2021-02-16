package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.addBinding;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.parseGeometryOrNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
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
  void parseGeometryOrNull_throwsException_forInCorrectInput() {
    // Arrange
    String testValue = "monkey";

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> parseGeometryOrNull(testValue));
  }

  @Test
  void addBinding_addStringBinding_default() {
    // Arrange
    IRI dataType = SimpleValueFactory.getInstance()
        .createIRI("http://www.opengis.net/ont/geosparql#Literal");
    PropertyShape propertyShape = PropertyShape.builder()
        .datatype(dataType)
        .build();
    Map<String, Function<BindingSet, Object>> assembleFns = new HashMap<>();
    String fieldName = "banana";
    String alias = "x1";

    // Act
    addBinding(assembleFns, alias, propertyShape, fieldName);

    // Assert
    assertThat(assembleFns.size(), is(1));
    assertThat(assembleFns.get(fieldName), is(notNullValue()));
  }

  @Test
  void addBinding_addGeoBinding_default() {
    // Arrange
    IRI dataType = SimpleValueFactory.getInstance()
        .createIRI("http://www.opengis.net/ont/geosparql#wktLiteral");
    PropertyShape propertyShape = PropertyShape.builder()
        .datatype(dataType)
        .build();
    Map<String, Function<BindingSet, Object>> assembleFns = new HashMap<>();
    String fieldName = "banana";
    String alias = "x1";

    // Act
    addBinding(assembleFns, alias, propertyShape, fieldName);

    // Assert
    assertThat(assembleFns.size(), is(1));
    assertThat(assembleFns.get(fieldName), is(notNullValue()));
  }
}
