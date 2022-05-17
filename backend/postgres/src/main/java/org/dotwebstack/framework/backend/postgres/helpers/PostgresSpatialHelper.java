package org.dotwebstack.framework.backend.postgres.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_BBOX;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.ARGUMENT_SRID;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.query.model.FieldRequest;

public final class PostgresSpatialHelper {

  private PostgresSpatialHelper() {}

  public static String getColumnName(PostgresSpatial spatial, Integer requestedSrid) {
    return getColumnName(spatial, requestedSrid, false);
  }

  public static String getColumnName(PostgresSpatial spatial, Integer requestedSrid, boolean isRequestedBbox) {
    Integer columnNameSrid = determineSridForColumnName(spatial, getSridOrDefault(spatial, requestedSrid));

    if (isRequestedBbox && spatial.getBboxes()
        .containsKey(columnNameSrid)) {
      return spatial.getBboxes()
          .get(columnNameSrid);
    }

    return spatial.getSpatialReferenceSystems()
        .get(columnNameSrid);
  }

  private static Integer getSridOrDefault(PostgresSpatial spatial, Integer requestedSrid) {
    return Optional.ofNullable(requestedSrid)
        .orElse(spatial.getSrid());
  }

  private static Integer determineSridForColumnName(PostgresSpatial spatial, Integer requestedSrid) {
    return determineSridFromSrs(spatial, requestedSrid).or(() -> determineSridFromEquivalents(spatial, requestedSrid))
        .orElseThrow(() -> requestValidationException("Srid {} is unknown. Valid srid values are {}.", requestedSrid,
            getValidSrids(spatial)));
  }

  private static Optional<Integer> determineSridFromSrs(PostgresSpatial spatial, Integer requestedSrid) {
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
    return getValueOfArgument(arguments, ARGUMENT_SRID, Integer.class);
  }

  public static Boolean isRequestedBbox(FieldRequest fieldRequest) {
    return getValueOfArgument(fieldRequest.getArguments(), ARGUMENT_BBOX, Boolean.class);
  }

  private static <T> T getValueOfArgument(Map<String, Object> arguments, String argumentKey, Class<T> returnType) {
    return Optional.ofNullable(arguments)
        .filter(args -> args.containsKey(argumentKey))
        .map(args -> args.get(argumentKey))
        .map(returnType::cast)
        .orElse(null);
  }
}
