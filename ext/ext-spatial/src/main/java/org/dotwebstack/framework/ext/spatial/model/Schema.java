package org.dotwebstack.framework.ext.spatial.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class Schema {

  @Valid
  private Spatial spatial;
}
