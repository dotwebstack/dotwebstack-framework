package org.dotwebstack.framework.backend.postgres.model;

import com.google.common.collect.BiMap;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostgresSpatial {

  @NotNull
  private Integer srid;

  private BiMap<Integer, String> spatialReferenceSystems;

  private BiMap<Integer, Integer> equivalents;
}
