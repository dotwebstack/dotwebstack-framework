package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

final class SpatialHelper {

  private SpatialHelper() {}

  public static String getColummName(String columnName, PostgresObjectField objectField, FieldRequest fieldRequest) {
    return Optional.ofNullable(getRequestedSrid(fieldRequest))
        .map(srid -> getColumnName(columnName, srid, objectField.getSpatialReferenceSystems()))
        .orElse(columnName);
  }

  private static String getColumnName(String columnName, Integer srid,
      Map<Integer, SpatialReferenceSystem> spatialReferenceSystems) {
    return Optional.ofNullable(spatialReferenceSystems.get(srid))
        .map(srs -> getSrsColumnName(columnName, srs))
        .orElseThrow(() -> illegalArgumentException("Srid {} is unknown. Valid srid values are {}.", srid,
            getValidSrids(spatialReferenceSystems)));
  }

  private static String getValidSrids(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems) {
    return spatialReferenceSystems.keySet()
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "));
  }

  private static String getSrsColumnName(String geometryColumn, SpatialReferenceSystem spatialReferenceSystem) {
    return Optional.ofNullable(((PostgresSpatialReferenceSystem) spatialReferenceSystem).getColumnSuffix())
        .map(geometryColumn::concat)
        .orElse(geometryColumn);
  }

  public static Integer getRequestedSrid(FieldRequest fieldRequest) {
    return Optional.ofNullable(fieldRequest.getArguments())
        .filter(arguments -> arguments.containsKey(SpatialConstants.SRID))
        .map(arguments -> arguments.get(SpatialConstants.SRID))
        .map(Integer.class::cast)
        .orElse(null);
  }
}
