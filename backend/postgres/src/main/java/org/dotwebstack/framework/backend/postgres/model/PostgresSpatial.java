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

  private boolean unifyInputGeometry;

  public boolean hasSegmentsTable() {
    return segmentsTable.isPresent();
  }

}
