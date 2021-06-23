package org.dotwebstack.framework.ext.spatial;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.locationtech.jts.geom.Geometry;

@Data
@SuperBuilder
public class GeometryFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  private final GeometryFilterOperator filterOperator;

  private final Geometry geometry;

  private final String crs;
}
