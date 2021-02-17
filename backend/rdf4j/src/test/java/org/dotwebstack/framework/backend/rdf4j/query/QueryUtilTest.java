package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.addBinding;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryUtil.parseGeometryOrNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class QueryUtilTest {

  @Test
  void parseGeometryOrNull_returnsNull_forNullValue() {
    String testValue = null;

    Geometry parsedValue = parseGeometryOrNull(testValue);

    assertThat(parsedValue, is(nullValue()));
  }

  @Test
  void parseGeometryOrNull_returnsGeometry_forCorrectInput() {
    String testValue = "LINESTRING (0 0, 0 10)";

    Geometry parsedValue = parseGeometryOrNull(testValue);

    assertThat(parsedValue, is(notNullValue()));
    assertThat(parsedValue.toString(), is(testValue));
  }

  @Test
  void parseGeometryOrNull_throwsException_forInCorrectInput() {
    String testValue = "monkey";

    assertThrows(IllegalArgumentException.class, () -> parseGeometryOrNull(testValue));
  }

  @Test
  void addBinding_addStringBinding_default() {
    IRI dataType = SimpleValueFactory.getInstance()
        .createIRI("http://www.w3.org/ns/shacl#Literal");
    PropertyShape propertyShape = PropertyShape.builder()
        .datatype(dataType)
        .build();
    Map<String, Function<BindingSet, Object>> assembleFns = new HashMap<>();
    String fieldName = "banana";
    String alias = "x1";
    String data = "data";

    BindingSet bindingSet = mock(BindingSet.class);
    Value value = mock(Value.class);

    when(bindingSet.getValue(alias)).thenReturn(value);
    when(value.stringValue()).thenReturn(data);

    addBinding(assembleFns, alias, propertyShape, fieldName);

    assertThat(assembleFns.size(), is(1));
    assertThat(assembleFns.get(fieldName), is(notNullValue()));
    assertThat(assembleFns.get(fieldName)
        .apply(bindingSet), is(data));

  }

  @Test
  void addBinding_addGeoBinding_default() {
    IRI dataType = SimpleValueFactory.getInstance()
        .createIRI("http://www.opengis.net/ont/geosparql#wktLiteral");
    PropertyShape propertyShape = PropertyShape.builder()
        .datatype(dataType)
        .build();
    Map<String, Function<BindingSet, Object>> assembleFns = new HashMap<>();
    String fieldName = "banana";
    String alias = "x1";
    String data = "POINT (5.979274334569982 52.21715768613606)";

    BindingSet bindingSet = mock(BindingSet.class);
    Value value = mock(Value.class);

    when(bindingSet.getValue(alias)).thenReturn(value);
    when(value.stringValue()).thenReturn(data);

    addBinding(assembleFns, alias, propertyShape, fieldName);

    assertThat(assembleFns.size(), is(1));
    assertThat(assembleFns.get(fieldName), is(notNullValue()));
    assertThat(assembleFns.get(fieldName)
        .apply(bindingSet)
        .toString(), is(data));
    assertThat(assembleFns.get(fieldName)
        .apply(bindingSet), instanceOf(Geometry.class));
  }
}
