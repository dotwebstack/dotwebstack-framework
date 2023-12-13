package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.collect.BiMap;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostgresSpatial {

  @NotNull
  private Integer srid;

  @Builder.Default
  private Optional<GeometrySegmentsTable> segmentsTable = Optional.empty();

  private BiMap<Integer, String> spatialReferenceSystems;

  private BiMap<Integer, Integer> equivalents;

  private BiMap<Integer, String> bboxes;

  // Flag to indicate the use of ST_UnaryUnion for ST_Intersects queries.
  // This is to avoid hitting a bug in PostGIS versions compiled with libgeos < 3.12.
  private boolean useSafeIntersects;

  public boolean hasSegmentsTable() {
    return segmentsTable.isPresent();
  }

}
