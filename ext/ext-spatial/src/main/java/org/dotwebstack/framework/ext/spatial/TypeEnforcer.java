package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.DEFAULT_SCALE;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.springframework.stereotype.Component;

@Component
public class TypeEnforcer {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  private final Map<Integer, GeometryPrecisionReducer> precisionReducers;

  private final GeometryPrecisionReducer defaultPrecisionReducer;

  public TypeEnforcer(Spatial spatial) {
    this.precisionReducers = Optional.of(spatial)
        .map(Spatial::getReferenceSystems)
        .map(this::getPrecisionReducers)
        .orElse(Map.of());

    this.defaultPrecisionReducer = createGeometryPrecisionReducer(DEFAULT_SCALE);
  }

  private Map<Integer, GeometryPrecisionReducer> getPrecisionReducers(
      Map<Integer, SpatialReferenceSystem> spatialReferenceSystems) {
    return spatialReferenceSystems.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .getScale() != null)
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), createGeometryPrecisionReducer(entry.getValue()
            .getScale())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private GeometryPrecisionReducer createGeometryPrecisionReducer(Integer scale) {
    return new GeometryPrecisionReducer(new PrecisionModel(Math.pow(10, scale)));
  }

  public Geometry enforce(GeometryType type, Geometry geometry) {
    var currentType = GeometryType.valueOf(geometry.getGeometryType()
        .toUpperCase());

    if (currentType.equals(type)) {
      return geometry;
    }

    return switch (type) {
      case POINT -> enforcePoint(geometry);
      case MULTIPOINT -> enforceMultiPoint(geometry);
      case MULTILINESTRING -> enforceMultiLineString(geometry);
      case MULTIPOLYGON -> enforceMultiPolygon(geometry);
      default -> throw ExceptionHelper.unsupportedOperationException("Enforcing to type '{}' is not supported!", type);
    };
  }

  private Point enforcePoint(Geometry geometry) {
    GeometryPrecisionReducer precisionReducer = Optional.ofNullable(precisionReducers.get(geometry.getSRID()))
        .orElse(defaultPrecisionReducer);

    var point = (Point) precisionReducer.reduce(geometry.getCentroid());
    point.setSRID(geometry.getSRID());
    return point;
  }

  private MultiPoint enforceMultiPoint(Geometry geometry) {
    if (geometry instanceof Point point) {
      var multiPoint = geometryFactory.createMultiPoint(new Point[] {point});
      multiPoint.setSRID(geometry.getSRID());
      return multiPoint;
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiPoint' not supported!",
        geometry.getClass()
            .getSimpleName());
  }

  private MultiLineString enforceMultiLineString(Geometry geometry) {
    if (geometry instanceof LineString lineString) {
      var multiLineString = geometryFactory.createMultiLineString(new LineString[] {lineString});
      multiLineString.setSRID(geometry.getSRID());
      return multiLineString;
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiLineString' not supported!",
        geometry.getClass()
            .getSimpleName());
  }

  private MultiPolygon enforceMultiPolygon(Geometry geometry) {
    if (geometry instanceof Polygon polygon) {
      var multiPolygon = geometryFactory.createMultiPolygon(new Polygon[] {polygon});
      multiPolygon.setSRID(geometry.getSRID());
      return multiPolygon;
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiPolygon' not supported!",
        geometry.getClass()
            .getSimpleName());
  }
}
