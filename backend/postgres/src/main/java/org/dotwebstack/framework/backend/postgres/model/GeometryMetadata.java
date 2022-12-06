package org.dotwebstack.framework.backend.postgres.model;

import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeometryMetadata {
  private Integer srid;

  private Optional<GeometrySegmentsTable> segmentsTable;
}
