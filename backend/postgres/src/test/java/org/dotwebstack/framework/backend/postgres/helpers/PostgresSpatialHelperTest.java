package org.dotwebstack.framework.backend.postgres.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.HashBiMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.junit.jupiter.api.Test;

class PostgresSpatialHelperTest {

  @Test
  void getColumnName_returnsColumnName_forRequestedSrid() {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getColummName(spatial, 7415);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_returnsColumnName_forNullSrid() {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getColummName(spatial, null);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_returnsColumnName_forEquivalentSrid() {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getColummName(spatial, 28992);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_throwsException_forMissingSpatialReferenceSystem() {
    PostgresSpatial spatial = createSpatial();

    var exception =
        assertThrows(IllegalArgumentException.class, () -> PostgresSpatialHelper.getColummName(spatial, 1234));

    assertThat(exception.getMessage(), is("Srid 1234 is unknown. Valid srid values are 7415, 28992."));
  }

  @Test
  void getSridOfColumnName_returnsSrid_whenColumnNameExists() {
    PostgresSpatial spatial = createSpatial();

    var result = PostgresSpatialHelper.getSridOfColumnName(spatial, "geometry");

    assertThat(result, is(7415));
  }

  @Test
  void etSridOfColumnName_throwsException_whenColumnNameDoesNotExist() {
    PostgresSpatial spatial = createSpatial();

    var exception =
        assertThrows(IllegalArgumentException.class, () -> PostgresSpatialHelper.getSridOfColumnName(spatial, "test"));

    assertThat(exception.getMessage(), is("Geometry column name test is unknown."));
  }

  @Test
  void getRequestedSrid_returnsSrid_whenArgumentsHasSrid() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(SpatialConstants.SRID, 28992))
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
        .spatialReferenceSystems(HashBiMap.create(Map.of(7415, "geometry")))
        .equivalents(HashBiMap.create(Map.of(7415, 28992)))
        .build();
  }
}
