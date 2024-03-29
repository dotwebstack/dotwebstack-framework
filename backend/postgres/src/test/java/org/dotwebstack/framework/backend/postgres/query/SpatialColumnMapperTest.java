package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.HashBiMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
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
    var mapper = createMapper(requestedSrid, false);

    var geom = createPoint(7415);

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
    var mapper = createMapper(requestedSrid, false);

    var geom = createPoint(geometrySrid);

    Map<String, Object> data = Map.of("testGeometry", geom);

    var exception = assertThrows(IllegalArgumentException.class, () -> mapper.apply(data));

    assertThat(exception.getMessage(),
        is(String.format("Can't reproject geometry from %s to %s.", geometrySrid, requestedSrid)));
  }

  private SpatialColumnMapper createMapper(Integer requestedSrid, boolean isRequestedBbox) {
    Field<Object> geometryColumn = DSL.field("testTable", "testGeometryColumn")
        .as("testGeometry");

    PostgresSpatial spatial = PostgresSpatial.builder()
        .srid(7415)
        .spatialReferenceSystems(HashBiMap.create(Map.of(7415, "geometry", 7931, "geometry_etrs89")))
        .equivalents(HashBiMap.create(Map.of(7415, 28992)))
        .bboxes(HashBiMap.create(Map.of(7415, "geometry_bbox")))
        .build();

    return new SpatialColumnMapper(geometryColumn, spatial, requestedSrid, isRequestedBbox);
  }

  private Geometry createPoint(Integer srid) {
    var geom = new GeometryFactory().createPoint(new Coordinate(5.97927433, 52.21715768, 10.2121));
    geom.setSRID(srid);
    return geom;
  }

  @Test
  void apply_returnsBboxGeometry_fromGeometry() {
    var requestedSrid = 7415;
    var mapper = createMapper(requestedSrid, true);

    var geom = createBboxPolygon(requestedSrid);

    var result = mapper.apply(Map.of("testGeometry", geom));

    assertThat(result, is(instanceOf(Geometry.class)));
    assertThat(((Geometry) result).getSRID(), is(requestedSrid));
    assertThat(((Geometry) result), is(createBboxPolygon(requestedSrid)));
  }

  @Test
  void apply_returnsBboxGeometry_fromGeometryEnvelope() {
    var requestedSrid = 7931;
    var mapper = createMapper(requestedSrid, true);

    var geom = createPolygon();

    var result = mapper.apply(Map.of("testGeometry", geom));

    assertThat(result, is(instanceOf(Geometry.class)));
    assertThat(((Geometry) result).getSRID(), is(requestedSrid));
    assertThat(((Geometry) result), is(createBboxPolygon(requestedSrid)));
  }

  private Geometry createPolygon() {
    Coordinate coordinateOne = new Coordinate(5.975167751312255, 52.18614182026113, 10.2121);
    Coordinate coordinateTwo = new Coordinate(5.9754252433776855, 52.18590501776668, 10.2121);
    Coordinate coordinateThree = new Coordinate(5.977141857147217, 52.18593132921723, 10.2121);
    Coordinate coordinateFour = new Coordinate(5.97731351852417, 52.186115508935124, 10.2121);
    Coordinate coordinateFive = new Coordinate(5.976927280426025, 52.186720665494036, 10.2121);
    Coordinate coordinateSix = new Coordinate(5.975167751312255, 52.18614182026113, 10.2121);

    var geom = new GeometryFactory().createPolygon(new Coordinate[] {coordinateOne, coordinateTwo, coordinateThree,
        coordinateFour, coordinateFive, coordinateSix});
    geom.setSRID(7931);
    return geom;
  }

  private Geometry createBboxPolygon(Integer srid) {
    Coordinate coordinateOne = new Coordinate(5.975167751312255, 52.18590501776668);
    Coordinate coordinateTwo = new Coordinate(5.975167751312255, 52.186720665494036);
    Coordinate coordinateThree = new Coordinate(5.97731351852417, 52.186720665494036);
    Coordinate coordinateFour = new Coordinate(5.97731351852417, 52.18590501776668);
    Coordinate coordinateFive = new Coordinate(5.975167751312255, 52.18590501776668);

    var geom = new GeometryFactory().createPolygon(
        new Coordinate[] {coordinateOne, coordinateTwo, coordinateThree, coordinateFour, coordinateFive});
    geom.setSRID(srid);
    return geom;
  }
}
