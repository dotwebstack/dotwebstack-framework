package org.dotwebstack.framework.ext.spatial.testhelper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.ext.spatial.model.AbstractSpatialReferenceSystem;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestSpatialReferenceSystem extends AbstractSpatialReferenceSystem {

  private String extraInfo;
}
