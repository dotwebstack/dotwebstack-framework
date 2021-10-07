package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.FROM_WKT;

import graphql.schema.GraphQLInputObjectField;
import java.util.Base64;
import java.util.Map;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.OperatorFilterCriteriaParser;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

@Component
public class GeometryFilterCriteriaParser extends OperatorFilterCriteriaParser {

  private final SpatialConfigurationProperties spatialConfigurationProperties;

  public GeometryFilterCriteriaParser(SpatialConfigurationProperties spatialConfigurationProperties) {
    this.spatialConfigurationProperties = spatialConfigurationProperties;
  }

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return SpatialConstants.GEOMETRY_FILTER.equals(TypeHelper.getTypeName(inputObjectField.getType()));
  }

  @Override
  protected FilterCriteria createFilterCriteria(FieldPath fieldPath, FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    switch (filterItem.getOperator()) {
      case SpatialConstants.INTERSECTS:
      case SpatialConstants.CONTAINS:
      case SpatialConstants.WITHIN:
        return createGeometryFilterCriteria(fieldPath, filterItem);
      default:
        return super.createFilterCriteria(fieldPath, fieldConfiguration, filterItem);
    }
  }

  @SuppressWarnings("unchecked")
  private GeometryFilterCriteria createGeometryFilterCriteria(FieldPath fieldPath, FilterItem filterItem) {
    if (!(filterItem.getValue() instanceof Map)) {
      throw illegalArgumentException("Filter item value not of type Map!");
    }

    var data = (Map<String, String>) filterItem.getValue();

    return GeometryFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterOperator(GeometryFilterOperator.valueOf(filterItem.getOperator()
            .toUpperCase()))
        .geometry(getGeometry(data))
        .crs(spatialConfigurationProperties.getSourceCrs())
        .build();
  }

  private Geometry getGeometry(Map<String, String> data) {
    if (countGeometryFilters(data) > 1) {
      throw illegalArgumentException(
        "The geometry filter can only contain one of the following methods: '%s', '%s' or '%s'.",
        FROM_WKT, FROM_WKB, FROM_GEOJSON
      );
    }
    if (data.containsKey(FROM_WKT)) {
      return getGeometryFromWkt(data.get(FROM_WKT));
    } else if (data.containsKey(FROM_WKB)) {
      return getGeometryFromWkb(data.get(FROM_WKB));
    } else if (data.containsKey(FROM_GEOJSON)) {
      return getGeometryFromGeoJson(data.get(FROM_GEOJSON));
    }

    throw illegalArgumentException(
      "The geometry filter does not contain one of the following methods: '%s', '%s' or '%s'.",
      FROM_WKT, FROM_WKB, FROM_GEOJSON
    );
  }

  private int countGeometryFilters(Map<String, String> data) {
    int counter = 0;
    for (String filter : new String[] { FROM_WKT, FROM_WKB, FROM_GEOJSON } ) {
      if (data.containsKey(filter)) {
        counter++;
      }
    }
    return counter;
  }

  private Geometry getGeometryFromWkt(String wkt) {
    var wktReader = new WKTReader();
    try {
      return wktReader.read(wkt);
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input WKT is invalid!", e);
    }
  }

  private Geometry getGeometryFromWkb(String wkb) {
    var wkbReader = new WKBReader();
    try {
      return wkbReader.read(Base64.getDecoder()
          .decode(wkb));
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input WKB is invalid!", e);
    }
  }

  private Geometry getGeometryFromGeoJson(String geoJson) {
    var geoJsonReader = new GeoJsonReader();
    try {
      return geoJsonReader.read(geoJson);
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input GeoJSON is invalid!", e);
    }
  }
}
