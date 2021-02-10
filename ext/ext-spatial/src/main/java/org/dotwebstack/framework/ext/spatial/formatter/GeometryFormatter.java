package org.dotwebstack.framework.ext.spatial.formatter;

import org.locationtech.jts.geom.Geometry;

public interface GeometryFormatter<T> {

  T format(Geometry geometry);
}