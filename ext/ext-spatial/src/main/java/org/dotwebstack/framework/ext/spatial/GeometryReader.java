package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID_RD;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID_RDNAP;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

@Component
public class GeometryReader {

  public static Geometry readGeometry(Map<String, String> data) {
    validateGeometryFilters(data);

    if (data.containsKey(FROM_WKT)) {
      return getGeometryFromWkt(data.get(FROM_WKT));
    } else if (data.containsKey(FROM_WKB)) {
      return getGeometryFromWkb(data.get(FROM_WKB));
    } else {
      return getGeometryFromGeoJson(data.get(FROM_GEOJSON));
    }
  }

  private static void validateGeometryFilters(Map<String, String> data) {
    var filters = List.of(FROM_WKT, FROM_WKB, FROM_GEOJSON);

    var foundFilters = data.keySet()
        .stream()
        .filter(filters::contains)
        .collect(Collectors.toList());

    if (foundFilters.isEmpty()) {
      throw illegalArgumentException("The geometry filter does not contain one of the following methods: %s.",
          String.join(" or ", filters));
    }

    if (foundFilters.size() > 1) {
      throw illegalArgumentException("The geometry filter can only contain one of the following methods: %s.",
          String.join(" or ", filters));
    }
  }

  private static Geometry getGeometryFromWkt(String wkt) {
    var wktReader = new WKTReader();
    try {
      var geometry = wktReader.read(wkt);
      if (getDimensionsFromGeometry(geometry) == 2) {
        geometry.setSRID(SRID_RD);
      } else {
        geometry.setSRID(SRID_RDNAP);
      }
      return geometry;
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input WKT is invalid!", e);
    }
  }

  private static int getDimensionsFromGeometry(Geometry geometry) {
    return Double.isNaN(geometry.getCoordinate()
        .getZ()) ? 2 : 3;
  }

  private static Geometry getGeometryFromWkb(String wkb) {
    var wkbReader = new WKBReader();
    try {
      return wkbReader.read(Base64.getDecoder()
          .decode(wkb));
    } catch (ParseException | IllegalArgumentException e) {
      throw illegalArgumentException("The filter input WKB is invalid!", e);
    }
  }

  private static Geometry getGeometryFromGeoJson(String geoJson) {
    var geoJsonReader = new GeoJsonReader();
    try {
      return geoJsonReader.read(geoJson);
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input GeoJSON is invalid!", e);
    }
  }
}
