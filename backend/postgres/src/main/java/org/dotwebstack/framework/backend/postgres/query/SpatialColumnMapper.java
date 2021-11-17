package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.jooq.Field;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

class SpatialColumnMapper extends ColumnMapper {

  private final PostgresSpatial spatial;

  private final Integer requestedSrid;

  private final boolean isRequestedBbox;

  public SpatialColumnMapper(Field<Object> column, PostgresSpatial spatial, Integer requestedSrid,
      boolean isRequestedBbox) {
    super(column);

    this.spatial = spatial;
    this.requestedSrid = requestedSrid;
    this.isRequestedBbox = isRequestedBbox;
  }

  @Override
  public Object apply(Map<String, Object> row) {
    Object value = super.apply(row);

    Geometry geometry = getGeometry(value).orElse(null);

    return Optional.ofNullable(geometry)
        .filter(this::mustReprojectGeometry)
        .map(this::reprojectGeometry)
        .orElse(geometry);
  }

  private Optional<Geometry> getGeometry(Object value) {
    return Optional.ofNullable(value)
        .filter(Geometry.class::isInstance)
        .map(Geometry.class::cast)
        .map(this::mapToRequestedGeometry);
  }

  private Geometry mapToRequestedGeometry(Geometry geometry) {
    if (mustReprojectToBbox(geometry)) {
      return getEnvelope(geometry);
    }
    return geometry;
  }

  private boolean mustReprojectToBbox(Geometry geometry) {
    return isRequestedBbox && spatial.getBboxes() != null && !spatial.getBboxes()
        .containsKey(geometry.getSRID());
  }

  private Geometry getEnvelope(Geometry geometry) {
    Geometry geom = geometry.getEnvelope()
        .copy();
    geom.setSRID(geometry.getSRID());
    return geom;
  }

  private boolean mustReprojectGeometry(Geometry geometry) {
    return requestedSrid != null && geometry.getSRID() != requestedSrid;
  }

  private Object reprojectGeometry(Geometry geometry) {
    Integer currentSrid = geometry.getSRID();

    return Optional.ofNullable(spatial.getEquivalents())
        .filter(equivalents -> equivalents.containsKey(currentSrid))
        .map(equivalents -> equivalents.get(currentSrid))
        .filter(equivalentSrid -> equivalentSrid.equals(requestedSrid))
        .map(srid -> reprojectTo2d(geometry))
        .orElseThrow(
            () -> illegalArgumentException("Can't reproject geometry from {} to {}.", currentSrid, requestedSrid));
  }

  private Geometry reprojectTo2d(Geometry geometry) {
    Geometry geometry2d = geometry.copy();
    geometry2d.setSRID(requestedSrid);

    Arrays.stream(geometry2d.getCoordinates())
        .forEach(c -> c.setCoordinate(new Coordinate(c.x, c.y)));

    return geometry2d;
  }
}
