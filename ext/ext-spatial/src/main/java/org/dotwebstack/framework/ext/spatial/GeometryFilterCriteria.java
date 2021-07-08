package org.dotwebstack.framework.ext.spatial;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.locationtech.jts.geom.Geometry;

@EqualsAndHashCode
@SuperBuilder
public class GeometryFilterCriteria implements FilterCriteria {
  private final FieldPath fieldPath;

  @Getter
  private final GeometryFilterOperator filterOperator;

  @Getter
  private final Geometry geometry;

  @Getter
  private final String crs;

  @Override
  public List<FieldPath> getFieldPaths() {
    return List.of(fieldPath);
  }
}
