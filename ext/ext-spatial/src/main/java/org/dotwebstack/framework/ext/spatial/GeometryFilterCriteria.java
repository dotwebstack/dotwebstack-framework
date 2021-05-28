package org.dotwebstack.framework.ext.spatial;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.locationtech.jts.geom.Geometry;

@Data
@Builder
public class GeometryFilterCriteria implements FilterCriteria {
  private final FieldConfiguration field;

  private final GeometryFilterOperation filterOperation;

  private final Geometry geometry;
}
