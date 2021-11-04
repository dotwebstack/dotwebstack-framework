package org.dotwebstack.framework.ext.spatial.model;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public abstract class AbstractSpatialReferenceSystem implements SpatialReferenceSystem {

  @NotNull
  private Integer dimensions;

  private Integer equivalent;
}
