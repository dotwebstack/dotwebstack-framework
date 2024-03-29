package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.DEFAULT_SCALE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

@AllArgsConstructor
public class SpatialDataFetcher implements DataFetcher<Object> {

  private final Spatial spatial;

  private final TypeEnforcer typeEnforcer;

  @Override
  public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
    if (Objects.isNull(dataFetchingEnvironment.getSource())) {
      return null;
    }

    if (!(dataFetchingEnvironment.getSource() instanceof Geometry)) {
      throw illegalArgumentException("Source is not an instance of Geometry");
    }

    var geometry = (Geometry) dataFetchingEnvironment.getSource();

    String fieldName = dataFetchingEnvironment.getFieldDefinition()
        .getName();

    String type = dataFetchingEnvironment.getExecutionStepInfo()
        .getParent()
        .getArgument(ARGUMENT_TYPE);

    if (type != null) {
      geometry = typeEnforcer.enforce(GeometryType.valueOf(type), geometry);
    }

    switch (fieldName) {
      case SRID:
        return geometry.getSRID();
      case TYPE:
        return geometry.getGeometryType()
            .toUpperCase();
      case AS_WKT:
        return createWkt(geometry);
      case AS_WKB:
        return createWkb(geometry);
      case AS_GEOJSON:
        return createGeoJson(geometry);
      default:
        throw illegalArgumentException("Invalid fieldName {}", fieldName);
    }
  }

  private String createWkt(Geometry geometry) {
    var wktWriter = new WKTWriter(getDimensionsFromGeometry(geometry));
    return wktWriter.write(geometry);
  }

  private String createWkb(Geometry geometry) {
    var wkbWriter = new WKBWriter(getDimensionsFromGeometry(geometry), true);
    return Base64.getEncoder()
        .encodeToString(wkbWriter.write(geometry));
  }

  private String createGeoJson(Geometry geometry) {
    var scale = getScale(geometry);
    var geoJsonWriter = new GeoJsonWriter(scale);
    geoJsonWriter.setEncodeCRS(false);
    return geoJsonWriter.write(geometry);
  }

  private int getScale(Geometry geometry) {
    return Optional.ofNullable(spatial)
        .map(Spatial::getReferenceSystems)
        .map(spatialReferenceSystems -> spatialReferenceSystems.get(geometry.getSRID()))
        .map(SpatialReferenceSystem::getScale)
        .orElse(DEFAULT_SCALE);
  }

  private int getDimensionsFromGeometry(Geometry geometry) {
    var coordinate = geometry.getCoordinate();
    if (coordinate != null) {
      return Double.isNaN(coordinate.getZ()) ? 2 : 3;
    }
    return 2;
  }
}
