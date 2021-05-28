package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.MapHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.ext.spatial.GeometryFilterCriteria;
import org.dotwebstack.framework.ext.spatial.GeometryFilterOperation;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

@Component
public class GeometryFilterCriteriaParser extends AbstractFilterCriteriaParser {

  @Override
  public boolean supports(GraphQLInputObjectField inputObjectField) {
    return SpatialConstants.GEOMETRY_FILTER.equals(TypeHelper.getTypeName(inputObjectField.getType()));
  }

  @Override
  public List<FilterCriteria> parse(TypeConfiguration<?> typeConfiguration, GraphQLInputObjectField inputObjectField,
      Map<String, Object> data) {
    return getFilter(typeConfiguration, inputObjectField, data).stream()
        .flatMap(filter -> getFilterItems(filter).stream())
        .map(geometryItem -> GeometryFilterCriteria.builder()
            .field(getFieldConfiguration(typeConfiguration, inputObjectField))
            .filterOperation(geometryItem.getFilterOperation())
            .geometry(geometryItem.getGeometry())
            .build())
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private List<GeometryItem> getFilterItems(Filter filter) {
    return getInputObjectTypes(filter.getInputObjectField())
        .flatMap(inputObjectType -> getFilterItems(inputObjectType, (Map<String, Object>) filter.getData()).stream())
        .collect(Collectors.toList());
  }

  private List<GeometryItem> getFilterItems(GraphQLInputObjectType inputObjectType, Map<String, Object> data) {
    return getInputObjectFields(inputObjectType)
        .filter(inputObjectField -> Objects.nonNull(data.get(inputObjectField.getName())))
        .map(inputObjectField -> createFilterItem(data, inputObjectField))
        .collect(Collectors.toList());
  }

  private GeometryItem createFilterItem(Map<String, Object> data, GraphQLInputObjectField inputObjectField) {
    return GeometryItem.builder()
        .filterOperation(GeometryFilterOperation.valueOf(inputObjectField.getName()
            .toUpperCase()))
        .geometry(readGeometry((String) MapHelper.getNestedMap(data, inputObjectField.getName())
            .get("fromWKT")))
        .build();
  }

  private Geometry readGeometry(String wkt) {
    WKTReader wktReader = new WKTReader();
    try {
      return wktReader.read(wkt);
    } catch (ParseException e) {
      throw illegalArgumentException("Unable to parse wkt!");
    }
  }

  @Data
  @Builder
  private static class GeometryItem {
    private GeometryFilterOperation filterOperation;

    private Geometry geometry;
  }

  private Stream<GraphQLInputObjectType> getInputObjectTypes(GraphQLInputObjectField inputObjectField) {
    return inputObjectField.getChildren()
        .stream()
        .filter(GraphQLInputObjectType.class::isInstance)
        .map(GraphQLInputObjectType.class::cast);
  }

  private Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(GraphQLInputObjectField.class::isInstance)
        .map(GraphQLInputObjectField.class::cast);
  }
}
