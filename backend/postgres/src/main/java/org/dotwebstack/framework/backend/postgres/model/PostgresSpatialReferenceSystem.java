package org.dotwebstack.framework.backend.postgres.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.ext.spatial.model.AbstractSpatialReferenceSystem;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresSpatialReferenceSystem extends AbstractSpatialReferenceSystem {

  private String columnSuffix;

  private String bboxColumnSuffix;
}
