package org.dotwebstack.framework.ext.spatial;

import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;

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

    Geometry geometry = dataFetchingEnvironment.getSource();
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
      default:
        throw unsupportedOperationException("Invalid fieldName {}", fieldName);
    }
  }

  private String createWkt(Geometry geometry) {
    WKTWriter wktWriter = new WKTWriter();
    return wktWriter.write(geometry);
  }

  private String createWkb(Geometry geometry) {
    WKBWriter wkbWriter = new WKBWriter();
    return encodeHexString(wkbWriter.write(geometry));
  }
}
