package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.collect.BiMap;
import java.util.Optional;
import javax.validation.constraints.NotNull;
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

  public boolean hasSegmentsTable() {
    return segmentsTable.isPresent();
  }

}
