package org.dotwebstack.framework.backend.postgres.helpers;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_BBOX;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_SRID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.HashBiMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.RequestValidationException;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PostgresSpatialHelperTest {

  @ParameterizedTest
  @CsvSource({"7415, false, geometry", "7415, true, geometry_bbox", "7931, false, geometry_etrs89",
      "7931, true, geometry_etrs89"})
  void getColumnName_returnsColumnName_forRequestedSrid(int srid, boolean isRequestedBbox, String expectedColumnName) {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getColumnName(spatial, srid, isRequestedBbox);

    assertThat(result, is(expectedColumnName));
  }

  @ParameterizedTest
  @CsvSource({"false, geometry", "true, geometry_bbox"})
  void getColumnName_returnsColumnName_forNullSrid(boolean isRequestedBbox, String expectedColumnName) {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getColumnName(spatial, null, isRequestedBbox);

    assertThat(result, is(expectedColumnName));
  }

  @Test
  void getColumnName_throwsException_forMissingSpatialReferenceSystem() {
    PostgresSpatial spatial = createSpatial();

    var exception =
        assertThrows(RequestValidationException.class, () -> PostgresSpatialHelper.getColumnName(spatial, 1234));

    assertThat(exception.getMessage(), is("Srid 1234 is unknown. Valid srid values are 7415, 7931, 9067, 28992."));
  }

  @Test
  void getSridOfColumnName_returnsSrid_whenColumnNameExists() {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getSridOfColumnName(spatial, "geometry");

    assertThat(result, is(7415));
  }

  @Test
  void getSridOfColumnName_throwsException_whenColumnNameDoesNotExist() {
    PostgresSpatial spatial = createSpatial();

    var exception =
        assertThrows(IllegalArgumentException.class, () -> PostgresSpatialHelper.getSridOfColumnName(spatial, "test"));

    assertThat(exception.getMessage(), is("Geometry column name test is unknown."));
  }

  @Test
  void getRequestedSrid_returnsSrid_whenArgumentsHasBbox() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(ARGUMENT_BBOX, true))
        .build();

    var result = PostgresSpatialHelper.isRequestedBbox(fieldRequest);

    assertThat(result, is(true));
  }

  @Test
  void getRequestedSrid_returnsNull_whenArgumentsHasNoBbox() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of())
        .build();

    var result = PostgresSpatialHelper.isRequestedBbox(fieldRequest);

    assertThat(result, is(nullValue()));
  }

  @Test
  void getRequestedSrid_returnsSrid_whenArgumentsHasSrid() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(ARGUMENT_SRID, 28992))
        .build();

    var result = PostgresSpatialHelper.getRequestedSrid(fieldRequest);

    assertThat(result, is(28992));
  }

  @Test
  void getRequestedSrid_returnsNull_whenArgumentsHasNoSrid() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of())
        .build();

    var result = PostgresSpatialHelper.getRequestedSrid(fieldRequest);

    assertThat(result, is(nullValue()));
  }

  private PostgresSpatial createSpatial() {
    return PostgresSpatial.builder()
        .srid(7415)
        .spatialReferenceSystems(HashBiMap.create(Map.of(7415, "geometry", 7931, "geometry_etrs89")))
        .equivalents(HashBiMap.create(Map.of(7415, 28992, 7931, 9067)))
        .bboxes(HashBiMap.create(Map.of(7415, "geometry_bbox")))
        .build();
  }
}
