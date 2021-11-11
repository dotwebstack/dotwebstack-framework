package org.dotwebstack.framework.backend.postgres.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;

public final class PostgresSpatialHelper {

  private PostgresSpatialHelper() {}

  public static String getColummName(PostgresSpatial spatial, Integer requestedSrid) {
    Integer columnNameSrid = determineSridForColumnName(spatial, getSridOrDefault(spatial, requestedSrid));

    return spatial.getSpatialReferenceSystems()
        .get(columnNameSrid);
  }

  private static Integer getSridOrDefault(PostgresSpatial spatial, Integer requestedSrid) {
    return Optional.ofNullable(requestedSrid)
        .orElse(spatial.getSrid());
  }

  private static Integer determineSridForColumnName(PostgresSpatial spatial, Integer requestedSrid) {
    return determineSridFromSRS(spatial, requestedSrid).or(() -> determineSridFromEquivalents(spatial, requestedSrid))
        .orElseThrow(() -> illegalArgumentException("Srid {} is unknown. Valid srid values are {}.", requestedSrid,
            getValidSrids(spatial)));
  }

  private static Optional<Integer> determineSridFromSRS(PostgresSpatial spatial, Integer requestedSrid) {
    return Optional.ofNullable(spatial.getSpatialReferenceSystems())
        .filter(spatialReferenceSystems -> spatialReferenceSystems.containsKey(requestedSrid))
        .map(spatialReferenceSystems -> requestedSrid);
  }

  private static Optional<Integer> determineSridFromEquivalents(PostgresSpatial spatial, Integer requestedSrid) {
    return Optional.ofNullable(spatial.getEquivalents())
        .filter(equivalents -> equivalents.inverse()
            .containsKey(requestedSrid))
        .map(equivalents -> equivalents.inverse()
            .get(requestedSrid));
  }

  private static String getValidSrids(PostgresSpatial spatial) {
    return Stream.concat(spatial.getSpatialReferenceSystems()
        .keySet()
        .stream(),
        spatial.getEquivalents()
            .values()
            .stream())
        .sorted(Comparator.naturalOrder())
        .map(Object::toString)
        .collect(Collectors.joining(", "));
  }

  public static Integer getSridOfColumnName(PostgresSpatial spatial, String columnName) {
    return Optional.ofNullable(spatial.getSpatialReferenceSystems())
        .filter(spatialReferenceSystems -> spatialReferenceSystems.inverse()
            .containsKey(columnName))
        .map(spatialReferenceSystems -> spatialReferenceSystems.inverse()
            .get(columnName))
        .orElseThrow(() -> illegalArgumentException("Geometry column name {} is unknown.", columnName));
  }

  public static Integer getRequestedSrid(FieldRequest fieldRequest) {
    return getRequestedSrid(fieldRequest.getArguments());
  }

  public static Integer getRequestedSrid(Map<String, Object> arguments) {
    return Optional.ofNullable(arguments)
        .filter(args -> args.containsKey(SpatialConstants.SRID))
        .map(args -> args.get(SpatialConstants.SRID))
        .map(Integer.class::cast)
        .orElse(null);
  }

}
