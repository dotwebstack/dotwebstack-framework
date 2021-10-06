package org.dotwebstack.framework.ext.spatial;

import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

public class SpatialDataFetcher implements DataFetcher<Object> {

  private final TypeEnforcer typeEnforcer;

  public SpatialDataFetcher(TypeEnforcer typeEnforcer) {
    this.typeEnforcer = typeEnforcer;
  }

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
        throw unsupportedOperationException("Invalid fieldName {}", fieldName);
    }
  }

  private String createWkt(Geometry geometry) {
    var wktWriter = new WKTWriter();
    return wktWriter.write(geometry);
  }

  private String createWkb(Geometry geometry) {
    var wkbWriter = new WKBWriter();
    return encodeHexString(wkbWriter.write(geometry));
  }

  private String createGeoJson(Geometry geometry) {
    var geoJsonWriter = new GeoJsonWriter();
    return geoJsonWriter.write(geometry);
  }
}
