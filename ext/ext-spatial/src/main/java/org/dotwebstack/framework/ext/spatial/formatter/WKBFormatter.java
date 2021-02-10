package org.dotwebstack.framework.ext.spatial.formatter;

import lombok.Builder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

@Builder
public final class WKBFormatter implements GeometryFormatter<byte[]> {

  private final int dimensions;

  @Override
  public byte[] format(Geometry geometry) {
    WKBWriter writer = new WKBWriter(dimensions);
    return writer.write(geometry);
  }
}