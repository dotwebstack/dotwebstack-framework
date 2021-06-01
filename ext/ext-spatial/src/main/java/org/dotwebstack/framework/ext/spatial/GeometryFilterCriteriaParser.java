package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLInputObjectField;
import java.util.Map;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.OperatorFilterCriteriaParser;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

@Component
public class GeometryFilterCriteriaParser extends OperatorFilterCriteriaParser {

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return SpatialConstants.GEOMETRY_FILTER.equals(TypeHelper.getTypeName(inputObjectField.getType()));
  }

  @Override
  protected FilterCriteria createFilterCriteria(FieldConfiguration fieldConfiguration, FilterItem filterItem) {
    switch (filterItem.getOperator()) {
      case SpatialConstants.INTERSECTS:
      case SpatialConstants.CONTAINS:
      case SpatialConstants.WITHIN:
        return createGeometryFilterCriteria(fieldConfiguration, filterItem);
      default:
        return super.createFilterCriteria(fieldConfiguration, filterItem);
    }
  }

  @SuppressWarnings("unchecked")
  private GeometryFilterCriteria createGeometryFilterCriteria(FieldConfiguration fieldConfiguration,
      FilterItem filterItem) {
    if (!(filterItem.getValue() instanceof Map)) {
      throw illegalArgumentException("Filter item value not of type Map!");
    }

    var data = (Map<String, String>) filterItem.getValue();

    return GeometryFilterCriteria.builder()
        .field(fieldConfiguration)
        .filterOperator(GeometryFilterOperator.valueOf(filterItem.getOperator()
            .toUpperCase()))
        .geometry(readGeometry(data.get(SpatialConstants.FROM_WKT)))
        .build();
  }

  private Geometry readGeometry(String wkt) {
    var wktReader = new WKTReader();
    try {
      return wktReader.read(wkt);
    } catch (ParseException e) {
      throw illegalArgumentException("The filter input WKT is invalid!", e);
    }
  }
}
