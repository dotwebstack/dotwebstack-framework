package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKT;

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

  private GeometryReader() {}

  public static Geometry readGeometry(Map<String, Object> data) {
    validateGeometryFilters(data);

    if (data.containsKey(FROM_WKT)) {
      return getGeometryFromWkt((String) data.get(FROM_WKT));
    } else if (data.containsKey(FROM_WKB)) {
      return getGeometryFromWkb((String) data.get(FROM_WKB));
    } else {
      return getGeometryFromGeoJson((String) data.get(FROM_GEOJSON));
    }
  }

  private static void validateGeometryFilters(Map<String, Object> data) {
    var filters = List.of(FROM_WKT, FROM_WKB, FROM_GEOJSON);

    var foundFilters = data.keySet()
        .stream()
        .filter(filters::contains)
        .collect(Collectors.toList());

    if (foundFilters.isEmpty()) {
      throw requestValidationException("The geometry filter does not contain one of the following methods: {}.",
          String.join(" or ", filters));
    }

    if (foundFilters.size() > 1) {
      throw requestValidationException("The geometry filter can only contain one of the following methods: {}.",
          String.join(" or ", filters));
    }
  }

  private static Geometry getGeometryFromWkt(String wkt) {
    var wktReader = new WKTReader();
    try {
      return wktReader.read(wkt);
    } catch (ParseException e) {
      throw requestValidationException("The filter input WKT is invalid!", e);
    }
  }

  private static Geometry getGeometryFromWkb(String wkb) {
    var wkbReader = new WKBReader();
    try {
      return wkbReader.read(Base64.getDecoder()
          .decode(wkb));
    } catch (ParseException | IllegalArgumentException e) {
      throw requestValidationException("The filter input WKB is invalid!", e);
    }
  }

  private static Geometry getGeometryFromGeoJson(String geoJson) {
    var geoJsonReader = new GeoJsonReader();
    try {
      return geoJsonReader.read(geoJson);
    } catch (ParseException e) {
      throw requestValidationException("The filter input GeoJSON is invalid!", e);
    }
  }
}
