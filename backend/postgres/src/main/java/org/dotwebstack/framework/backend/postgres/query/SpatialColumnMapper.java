package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.jooq.Field;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

class SpatialColumnMapper extends ColumnMapper {

  private final Map<Integer, SpatialReferenceSystem> spatialReferenceSystems;

  private final Integer requestedSrid;

  public SpatialColumnMapper(Field<Object> column, Map<Integer, SpatialReferenceSystem> spatialReferenceSystems,
      Integer requestedSrid) {
    super(column);

    this.spatialReferenceSystems = spatialReferenceSystems;
    this.requestedSrid = requestedSrid;
  }

  @Override
  public Object apply(Map<String, Object> row) {
    Object value = super.apply(row);

    return getGeometry(value).filter(this::mustReprojectGeometry)
        .map(this::reprojectGeometry)
        .orElse(value);
  }

  private Optional<Geometry> getGeometry(Object value) {
    return Optional.ofNullable(value)
        .filter(Geometry.class::isInstance)
        .map(Geometry.class::cast);
  }

  private boolean mustReprojectGeometry(Geometry geometry) {
    return requestedSrid != null && geometry.getSRID() != requestedSrid;
  }

  private Object reprojectGeometry(Geometry geometry) {
    int currentSrid = geometry.getSRID();

    SpatialReferenceSystem currentSrs = spatialReferenceSystems.get(currentSrid);

    return Optional.ofNullable(currentSrs.getEquivalent())
        .filter(requestedSrid::equals)
        .map(srid -> reprojectTo2d(geometry, srid))
        .orElseThrow(
            () -> illegalArgumentException("Can't reproject geometry from {} to {}.", currentSrid, requestedSrid));
  }

  private Object reprojectTo2d(Geometry geometry, Integer srid) {
    Geometry geometry2d = geometry.copy();
    geometry2d.setSRID(srid);

    Arrays.stream(geometry2d.getCoordinates())
        .forEach(c -> c.setCoordinate(new Coordinate(c.x, c.y)));

    return geometry2d;
  }
}
