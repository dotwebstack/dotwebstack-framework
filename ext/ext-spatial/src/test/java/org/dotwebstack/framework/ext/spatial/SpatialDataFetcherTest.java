package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.GeometryType.POINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialDataFetcherTest {

  private final String geometryString = "POINT (5.979274334569982 52.21715768613606)";

  private SpatialDataFetcher spatialDataFetcher;

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironment;

  @Mock
  private ExecutionStepInfo executionStepInfo;

  private Geometry geometry;

  @Mock
  private GraphQLFieldDefinition fieldDefinition;

  @Mock
  private TypeEnforcer typeEnforcer;

  @BeforeEach
  void beforeAll() throws ParseException {
    spatialDataFetcher = new SpatialDataFetcher(typeEnforcer);

    WKTReader reader = new WKTReader();
    geometry = reader.read(geometryString);
  }

  @Test
  void get_returnsNull_sourceNull() {
    when(dataFetchingEnvironment.getSource()).thenReturn(null);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(nullValue()));
  }

  @Test
  void get_returnsNull_notGeometry() {
    when(dataFetchingEnvironment.getSource()).thenReturn(mock(SpatialConfigurer.class));

    assertThrows(IllegalArgumentException.class, () -> spatialDataFetcher.get(dataFetchingEnvironment));
  }

  @Test
  void get_returnsValue_forType() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn(TYPE);
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(notNullValue()));
    assertThat(value, instanceOf(String.class));
    String stringValue = (String) value;
    assertThat(stringValue, is("POINT"));
  }

  @Test
  void get_callsTypeEnforcer_forDefault() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(executionStepInfo.getArgument("type")).thenReturn("POINT");
    when(typeEnforcer.enforce(eq(POINT), any(Geometry.class))).thenReturn(geometry);
    when(fieldDefinition.getName()).thenReturn(AS_WKT);
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(notNullValue()));
    assertThat(value, instanceOf(String.class));
    String stringValue = (String) value;
    assertThat(stringValue, is(geometryString));
    verify(typeEnforcer, times(1)).enforce(any(), any());
  }

  @Test
  void get_returnsValue_forAsWkt() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn(AS_WKT);
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(notNullValue()));
    assertThat(value, instanceOf(String.class));
    String stringValue = (String) value;
    assertThat(stringValue, is(geometryString));
    verify(typeEnforcer, times(0)).enforce(any(), any());
  }

  @Test
  void get_returnsValue_forAsWkb() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn(AS_WKB);
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(notNullValue()));
    assertThat(value, instanceOf(String.class));
    String stringValue = (String) value;
    assertThat(stringValue, is("00000000014017eac6e4232933404a1bcbd2b403c4"));
  }

  @Test
  void get_returnsValue_forAsGeoJson() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn(AS_GEOJSON);
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    Object value = spatialDataFetcher.get(dataFetchingEnvironment);

    assertThat(value, is(notNullValue()));
    assertThat(value, instanceOf(String.class));
    String stringValue = (String) value;
    assertThat(stringValue, is("{\"type\":\"Point\",\"coordinates\":[5.97927433,52.21715769],\"crs\":"
        + "{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:0\"}}}"));
  }

  @Test
  void get_throwsException_forUnknown() {
    when(dataFetchingEnvironment.getSource()).thenReturn(geometry);
    when(dataFetchingEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);
    when(fieldDefinition.getName()).thenReturn("monkey");
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getParent()).thenReturn(executionStepInfo);

    assertThrows(UnsupportedOperationException.class, () -> spatialDataFetcher.get(dataFetchingEnvironment));
  }
}
