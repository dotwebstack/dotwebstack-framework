package org.dotwebstack.framework.ext.spatial;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.AbstractFilterCriteria;
import org.locationtech.jts.geom.Geometry;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GeometryFilterCriteria extends AbstractFilterCriteria {
  private final FieldConfiguration field;

  private final GeometryFilterOperator filterOperator;

  private final Geometry geometry;
}
