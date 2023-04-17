package org.dotwebstack.framework.ext.spatial.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public abstract class AbstractSpatialReferenceSystem implements SpatialReferenceSystem {

  @NotNull
  private Integer dimensions;

  private Integer equivalent;

  private Integer scale;
}
