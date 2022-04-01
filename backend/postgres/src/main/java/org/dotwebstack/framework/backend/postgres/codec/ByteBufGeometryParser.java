package org.dotwebstack.framework.backend.postgres.codec;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.postgis.binary.ByteGetter;
import org.postgis.binary.ValueGetter;
import org.postgis.jts.JtsBinaryParser;
import org.springframework.stereotype.Component;

@Component
class ByteBufGeometryParser extends JtsBinaryParser {

  private final JtsSpatialContextFactory jtsFactory = new JtsSpatialContextFactory();

  public Geometry parse(ByteBuf byteBuf) {
    var byteGetter = new ByteBufByteGetter(byteBuf);
    return parseGeometry(valueGetterForEndian(byteGetter));
  }

  // This method and the methods it calls are copied from JtsBinaryParser. In actuality, only
  // parsePolygon(ValueGetter, boolean, boolean, int) has changes to handle empty geometries.
  protected Geometry parseGeometry(ValueGetter data) {
    return parseGeometry(data, 0, false);
  }

  protected Geometry parseGeometry(ValueGetter data, int srid, boolean inheritSrid) {
    byte endian = data.getByte(); // skip and test endian flag
    if (endian != data.endian) {
      throw new IllegalArgumentException("Endian inconsistency!");
    }
    int typeword = data.getInt();

    int realtype = typeword & 0x1FFFFFFF; // cut off high flag bits

    boolean haveZ = (typeword & 0x80000000) != 0;
    boolean haveM = (typeword & 0x40000000) != 0;
    boolean haveS = (typeword & 0x20000000) != 0;

    if (haveS) {
      int newsrid = org.postgis.Geometry.parseSRID(data.getInt());
      if (inheritSrid && newsrid != srid) {
        throw new IllegalArgumentException("Inconsistent srids in complex geometry: " + srid + ", " + newsrid);
      } else {
        srid = newsrid;
      }
    } else if (!inheritSrid) {
      srid = org.postgis.Geometry.UNKNOWN_SRID;
    }

    Geometry result;
    switch (realtype) {
      case org.postgis.Geometry.POINT:
        result = parsePoint(data, haveZ, haveM);
        break;
      case org.postgis.Geometry.LINESTRING:
        result = parseLineString(data, haveZ, haveM);
        break;
      case org.postgis.Geometry.POLYGON:
        result = parsePolygon(data, haveZ, haveM, srid);
        break;
      case org.postgis.Geometry.MULTIPOINT:
        result = parseMultiPoint(data, srid);
        break;
      case org.postgis.Geometry.MULTILINESTRING:
        result = parseMultiLineString(data, srid);
        break;
      case org.postgis.Geometry.MULTIPOLYGON:
        result = parseMultiPolygon(data, srid);
        break;
      case org.postgis.Geometry.GEOMETRYCOLLECTION:
        result = parseCollection(data, srid);
        break;
      default:
        throw new IllegalArgumentException("Unknown Geometry Type!");
    }

    result.setSRID(srid);

    return result;
  }

  private Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM) {
    double x = data.getDouble();
    double y = data.getDouble();
    Point result;
    if (haveZ) {
      double z = data.getDouble();
      result = jtsFactory.getGeometryFactory()
          .createPoint(new Coordinate(x, y, z));
    } else {
      result = jtsFactory.getGeometryFactory()
          .createPoint(new Coordinate(x, y));
    }

    if (haveM) { // skip M value
      data.getDouble();
    }

    return result;
  }

  private void parseGeometryArray(ValueGetter data, Geometry[] container, int srid) {
    for (int i = 0; i < container.length; i++) {
      container[i] = parseGeometry(data, srid, true);
    }
  }

  private CoordinateSequence parseCS(ValueGetter data, boolean haveZ, boolean haveM) {
    int count = data.getInt();
    int dims = haveZ ? 3 : 2;
    CoordinateSequence cs = new PackedCoordinateSequence.Double(count, dims, 0);

    for (int i = 0; i < count; i++) {
      for (int d = 0; d < dims; d++) {
        cs.setOrdinate(i, d, data.getDouble());
      }
      if (haveM) { // skip M value
        data.getDouble();
      }
    }
    return cs;
  }

  private MultiPoint parseMultiPoint(ValueGetter data, int srid) {
    Point[] points = new Point[data.getInt()];
    parseGeometryArray(data, points, srid);
    return jtsFactory.getGeometryFactory()
        .createMultiPoint(points);
  }


  private LineString parseLineString(ValueGetter data, boolean haveZ, boolean haveM) {
    return jtsFactory.getGeometryFactory()
        .createLineString(parseCS(data, haveZ, haveM));
  }

  private LinearRing parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM) {
    return jtsFactory.getGeometryFactory()
        .createLinearRing(parseCS(data, haveZ, haveM));
  }

  private Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM, int srid) {
    int holecount = data.getInt() - 1;
    if (holecount < 0) {
      var empty = jtsFactory.getGeometryFactory()
          .createPolygon();
      empty.setSRID(srid);
      return empty;
    }
    LinearRing[] rings = new LinearRing[holecount];
    LinearRing shell = parseLinearRing(data, haveZ, haveM);
    shell.setSRID(srid);
    for (int i = 0; i < holecount; i++) {
      rings[i] = parseLinearRing(data, haveZ, haveM);
      rings[i].setSRID(srid);
    }
    return jtsFactory.getGeometryFactory()
        .createPolygon(shell, rings);
  }

  private MultiLineString parseMultiLineString(ValueGetter data, int srid) {
    int count = data.getInt();
    LineString[] strings = new LineString[count];
    parseGeometryArray(data, strings, srid);
    return jtsFactory.getGeometryFactory()
        .createMultiLineString(strings);
  }

  private MultiPolygon parseMultiPolygon(ValueGetter data, int srid) {
    int count = data.getInt();
    Polygon[] polys = new Polygon[count];
    parseGeometryArray(data, polys, srid);
    return jtsFactory.getGeometryFactory()
        .createMultiPolygon(polys);
  }

  private GeometryCollection parseCollection(ValueGetter data, int srid) {
    int count = data.getInt();
    Geometry[] geoms = new Geometry[count];
    parseGeometryArray(data, geoms, srid);
    return jtsFactory.getGeometryFactory()
        .createGeometryCollection(geoms);
  }

  @RequiredArgsConstructor
  public static class ByteBufByteGetter extends ByteGetter {

    private final ByteBuf byteBuf;

    @Override
    public int get(int i) {
      return byteBuf.getByte(i) & 0xFF;
    }
  }
}
