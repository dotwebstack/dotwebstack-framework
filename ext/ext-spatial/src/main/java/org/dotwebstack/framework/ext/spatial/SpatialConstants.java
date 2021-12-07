package org.dotwebstack.framework.ext.spatial;

public class SpatialConstants {

  static final int DEFAULT_SCALE = 8;

  private SpatialConstants() {}

  // ObjectTypes

  public static final String GEOMETRY = "Geometry";

  static final String GEOMETRY_INPUT = "GeometryInput";

  public static final String GEOMETRY_FILTER = "GeometryFilter";

  static final String GEOMETRY_TYPE = "GeometryType";

  // ArgumentNames

  public static final String ARGUMENT_TYPE = "type";

  public static final String ARGUMENT_BBOX = "bbox";

  public static final String ARGUMENT_SRID = "srid";

  // FieldNames

  static final String TYPE = "type";

  static final String SRID = "srid";

  static final String AS_WKT = "asWKT";

  static final String AS_WKB = "asWKB";

  static final String AS_GEOJSON = "asGeoJSON";

  public static final String FROM_WKT = "fromWKT";

  public static final String FROM_GEOJSON = "fromGeoJSON";

  public static final String FROM_WKB = "fromWKB";

  public static final String WITHIN = "within";

  public static final String CONTAINS = "contains";

  public static final String INTERSECTS = "intersects";

  // GeometryTypes

  static final String POINT = "POINT";

  static final String LINESTRING = "LINESTRING";

  static final String POLYGON = "POLYGON";

  static final String MULTIPOINT = "MULTIPOINT";

  static final String MULTILINESTRING = "MULTILINESTRING";

  static final String MULTIPOLYGON = "MULTIPOLYGON";

  static final String GEOMETRYCOLLECTION = "GEOMETRYCOLLECTION";
}
