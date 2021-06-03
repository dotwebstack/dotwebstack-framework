package org.dotwebstack.framework.ext.spatial;

public class SpatialConstants {

  private SpatialConstants() {}

  // ObjectTypes

  static final String GEOMETRY = "Geometry";

  static final String GEOMETRY_INPUT = "GeometryInput";

  public static final String GEOMETRY_FILTER = "GeometryFilter";

  static final String GEOMETRY_TYPE = "GeometryType";

  // ArgumentNames

  static final String ARGUMENT_TYPE = "type";

  // FieldNames

  static final String TYPE = "type";

  static final String AS_WKT = "asWKT";

  static final String AS_WKB = "asWKB";

  static final String FROM_WKT = "fromWKT";

  static final String WITHIN = "within";

  static final String CONTAINS = "contains";

  static final String INTERSECTS = "intersects";

  // GeometryTypes

  static final String POINT = "POINT";

  static final String LINESTRING = "LINESTRING";

  static final String POLYGON = "POLYGON";

  static final String MULTIPOINT = "MULTIPOINT";

  static final String MULTILINESTRING = "MULTILINESTRING";

  static final String MULTIPOLYGON = "MULTIPOLYGON";
}
