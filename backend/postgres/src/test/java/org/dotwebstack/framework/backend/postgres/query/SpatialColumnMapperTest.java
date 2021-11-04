package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

class SpatialColumnMapperTest {

  @Test
  void apply_returnsGeometry_withNoReprojection() {
    apply_returnsGeometry(7415, 7415, 10.2121);
  }

  @Test
  void apply_returnsGeometry_withNoRequestedSrid() {
    apply_returnsGeometry(null, 7415, 10.2121);
  }

  @Test
  void apply_returnsGeometry_withReprojection() {
    apply_returnsGeometry(28992, 28992, Double.NaN);
  }

  @Test
  void apply_throwsException_forMissingEquivalent() {
    apply_throwsException(9067, 7931);
  }

  @Test
  void apply_throwsException_forMismatchEquivalent() {
    apply_throwsException(7931, 7415);
  }

  private void apply_returnsGeometry(Integer requestedSrid, Integer expectedSrid, Double expectedZ) {
    var mapper = createMapper(requestedSrid);

    var geom = createGeometry(7415);

    var result = mapper.apply(Map.of("testGeometry", geom));

    assertThat(result, is(instanceOf(Geometry.class)));
    assertThat(((Geometry) result).getCoordinate()
        .getX(), is(5.97927433));
    assertThat(((Geometry) result).getCoordinate()
        .getY(), is(52.21715768));
    assertThat(((Geometry) result).getCoordinate()
        .getZ(), is(expectedZ));
    assertThat(((Geometry) result).getSRID(), is(expectedSrid));
  }

  private void apply_throwsException(Integer requestedSrid, Integer geometrySrid) {
    var mapper = createMapper(requestedSrid);

    var geom = createGeometry(geometrySrid);

    Map<String, Object> data = Map.of("testGeometry", geom);

    var exception = assertThrows(IllegalArgumentException.class, () -> mapper.apply(data));

    assertThat(exception.getMessage(),
        is(String.format("Can't reproject geometry from %s to %s.", geometrySrid, requestedSrid)));
  }

  private SpatialColumnMapper createMapper(Integer requestedSrid) {
    Field<Object> geometryColumn = DSL.field("testTable", "testGeometryColumn")
        .as("testGeometry");
    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = createSpatialReferenceSystems();

    return new SpatialColumnMapper(geometryColumn, spatialReferenceSystems, requestedSrid);
  }

  private Geometry createGeometry(Integer srid) {
    var geom = new GeometryFactory().createPoint(new Coordinate(5.97927433, 52.21715768, 10.2121));
    geom.setSRID(srid);
    return geom;
  }

  private Map<Integer, SpatialReferenceSystem> createSpatialReferenceSystems() {
    Map<Integer, SpatialReferenceSystem> srsMap = new LinkedHashMap<>();
    srsMap.put(7415, createSpatialReferenceSystem(28992));
    srsMap.put(28992, createSpatialReferenceSystem(null));
    srsMap.put(7931, createSpatialReferenceSystem(null));

    return srsMap;
  }

  private PostgresSpatialReferenceSystem createSpatialReferenceSystem(Integer equivalent) {
    var srs = new PostgresSpatialReferenceSystem();
    srs.setEquivalent(equivalent);
    return srs;
  }
}
