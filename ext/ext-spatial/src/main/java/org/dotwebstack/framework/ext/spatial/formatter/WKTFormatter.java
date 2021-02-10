package org.dotwebstack.framework.ext.spatial.formatter;

import lombok.Builder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

@Builder
public final class WKTFormatter implements GeometryFormatter<String> {

  private final int dimensions;

  @Override
  public String format(Geometry geometry) {
    WKTWriter writer = new WKTWriter(dimensions);
    return writer.write(geometry);
  }
}