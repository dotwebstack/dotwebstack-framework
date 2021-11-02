package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.junit.jupiter.api.Test;

class SpatialHelperTest {

  @Test
  void getColumnName_returnsColumnName_forGivenArgument() {
    var columnName = "geometry";

    PostgresObjectField postgresObjectField = createPostgresObjectField();

    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(SpatialConstants.SRID, 7415))
        .build();

    var result = SpatialHelper.getColummName(columnName, postgresObjectField, fieldRequest);

    assertThat(result, is("geometry_7415"));
  }

  @Test
  void getColumnName_returnsColumnName_forNoColumnPrefix() {
    var columnName = "geometry";

    PostgresObjectField postgresObjectField = createPostgresObjectField();

    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(SpatialConstants.SRID, 28992))
        .build();

    var result = SpatialHelper.getColummName(columnName, postgresObjectField, fieldRequest);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_returnsColumnName_forNoArguments() {
    var columnName = "geometry";
    var postgresObjectField = new PostgresObjectField();
    var fieldRequest = FieldRequest.builder()
        .build();

    var result = SpatialHelper.getColummName(columnName, postgresObjectField, fieldRequest);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_returnsColumnName_forNoSridArgument() {
    var columnName = "geometry";
    var postgresObjectField = new PostgresObjectField();
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of("test", "test"))
        .build();

    var result = SpatialHelper.getColummName(columnName, postgresObjectField, fieldRequest);

    assertThat(result, is("geometry"));
  }

  @Test
  void getColumnName_throwsException_forMissingSpatialReferenceSystem() {
    var columnName = "geometry";
    PostgresObjectField postgresObjectField = createPostgresObjectField();
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(SpatialConstants.SRID, 1234))
        .build();

    var exception = assertThrows(IllegalArgumentException.class,
        () -> SpatialHelper.getColummName(columnName, postgresObjectField, fieldRequest));

    assertThat(exception.getMessage(), is("Srid 1234 is unknown. Valid srid values are 7415, 28992."));
  }

  @Test
  void getRequestedSrid_returnsSrid_whenArgumentsHasSrid() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of(SpatialConstants.SRID, 28992))
        .build();

    var result = SpatialHelper.getRequestedSrid(fieldRequest);

    assertThat(result, is(28992));
  }

  @Test
  void getRequestedSrid_returnsNull_whenArgumentsHasNoSrid() {
    var fieldRequest = FieldRequest.builder()
        .arguments(Map.of())
        .build();

    var result = SpatialHelper.getRequestedSrid(fieldRequest);

    assertThat(result, is(nullValue()));
  }

  private PostgresObjectField createPostgresObjectField() {
    var postgresObjectField = new PostgresObjectField();

    Map<Integer, SpatialReferenceSystem> srsMap = new LinkedHashMap<>();
    srsMap.put(7415, createSpatialReferenceSystem("_7415"));
    srsMap.put(28992, createSpatialReferenceSystem(null));
    postgresObjectField.setSpatialReferenceSystems(srsMap);

    return postgresObjectField;
  }

  private PostgresSpatialReferenceSystem createSpatialReferenceSystem(String columnSuffix) {
    var srs = new PostgresSpatialReferenceSystem();
    srs.setColumnSuffix(columnSuffix);
    return srs;
  }
}
