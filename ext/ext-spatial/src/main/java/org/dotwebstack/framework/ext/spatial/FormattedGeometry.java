package org.dotwebstack.framework.ext.spatial;

import org.dotwebstack.framework.ext.spatial.formatter.GeometryFormatter;
import org.locationtech.jts.geom.Geometry;

public final class FormattedGeometry {

  private final Geometry geometry;

  private final GeometryFormatter<?> formatter;

  public FormattedGeometry(Geometry geometry, GeometryFormatter<?> formatter) {
    this.geometry = geometry;
    this.formatter = formatter;
  }

  public Object get() {
    return formatter.format(geometry);
  }
}
