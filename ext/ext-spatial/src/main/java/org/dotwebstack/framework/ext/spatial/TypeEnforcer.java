package org.dotwebstack.framework.ext.spatial;

import org.dotwebstack.framework.core.helpers.ExceptionHelper;
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

  private static final GeometryPrecisionReducer precisionReducer =
      new GeometryPrecisionReducer(new PrecisionModel(1000));

  public Geometry enforce(GeometryType type, Geometry geometry) {
    var currentType = GeometryType.valueOf(geometry.getGeometryType()
        .toUpperCase());

    if (currentType.equals(type)) {
      return geometry;
    }

    switch (type) {
      case POINT:
        return enforcePoint(geometry);
      case MULTIPOINT:
        return enforceMultiPoint(geometry);
      case MULTILINESTRING:
        return enforceMultiLineString(geometry);
      case MULTIPOLYGON:
        return enforceMultiPolygon(geometry);
      default:
        throw ExceptionHelper.unsupportedOperationException("Enforcing to type '{}' is not supported!", type);
    }
  }

  private Point enforcePoint(Geometry geometry) {
    return (Point) precisionReducer.reduce(geometry.getCentroid());
  }

  private MultiPoint enforceMultiPoint(Geometry geometry) {
    if (geometry instanceof Point) {
      return geometryFactory.createMultiPoint(new Point[] {(Point) geometry});
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiPoint' not supported!",
        geometry.getClass()
            .getSimpleName());
  }

  private MultiLineString enforceMultiLineString(Geometry geometry) {
    if (geometry instanceof LineString) {
      return geometryFactory.createMultiLineString(new LineString[] {(LineString) geometry});
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiLineString' not supported!",
        geometry.getClass()
            .getSimpleName());
  }

  private MultiPolygon enforceMultiPolygon(Geometry geometry) {
    if (geometry instanceof Polygon) {
      return geometryFactory.createMultiPolygon(new Polygon[] {(Polygon) geometry});
    }

    throw ExceptionHelper.unsupportedOperationException("Enforcing '{}' to 'MultiPolygon' not supported!",
        geometry.getClass()
            .getSimpleName());
  }
}
