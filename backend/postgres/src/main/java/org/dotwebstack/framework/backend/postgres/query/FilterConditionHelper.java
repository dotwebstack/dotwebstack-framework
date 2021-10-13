package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.TableHelper.findTable;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.dotwebstack.framework.ext.spatial.GeometryFilterCriteria;
import org.dotwebstack.framework.ext.spatial.SpatialConfigurationProperties;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
public class FilterConditionHelper {

  private static final DataType<Geometry> GEOMETRY_DATATYPE =
      new DefaultDataType<>(SQLDialect.POSTGRES, Geometry.class, "geometry");

  private final DSLContext dslContext;

  private final JoinHelper joinHelper;

  public FilterConditionHelper(DSLContext dslContext, JoinHelper joinHelper) {
    this.dslContext = dslContext;
    this.joinHelper = joinHelper;
  }

  public Condition createCondition(FilterCriteria filterCriteria, Table<?> table,
      ObjectSelectContext objectSelectContext, ObjectRequest objectRequest) {
    if (filterCriteria instanceof NotFilterCriteria) {
      return createCondition((NotFilterCriteria) filterCriteria, table, objectSelectContext, objectRequest);
    }

    return createCondition(filterCriteria.getFieldPath(), filterCriteria, objectSelectContext,
        objectRequest.getContextCriterias(), (PostgresTypeConfiguration) objectRequest.getTypeConfiguration(), table);
  }

  private Condition createCondition(NotFilterCriteria notFilterCriteria, Table<?> table,
      ObjectSelectContext objectSelectContext, ObjectRequest objectRequest) {
    var innerCondition =
        createCondition(notFilterCriteria.getFilterCriteria(), table, objectSelectContext, objectRequest);
    return DSL.not(innerCondition);
  }

  private Condition createCondition(FieldPath fieldPath, FilterCriteria filterCriteria,
      ObjectSelectContext objectSelectContext, List<ContextCriteria> contextCriterias,
      PostgresTypeConfiguration typeConfiguration, Table<?> fromTable) {
    // recursief
    if (fieldPath.isNode() && !fieldPath.getFieldConfiguration()
        .isNestedObjectField()) {
      var subQuery =
          buildQuery(fieldPath, filterCriteria, fromTable, objectSelectContext, typeConfiguration, contextCriterias);

      return DSL.exists(subQuery);
    } else { // leaf
      return createCondition(filterCriteria, fromTable.getName());
    }
  }

  private Condition createCondition(FilterCriteria filterCriteria, String fromTable) {
    if (filterCriteria instanceof EqualsFilterCriteria) {
      return createCondition((EqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof GreaterThenFilterCriteria) {
      return createCondition((GreaterThenFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof GreaterThenEqualsFilterCriteria) {
      return createCondition((GreaterThenEqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof LowerThenFilterCriteria) {
      return createCondition((LowerThenFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof LowerThenEqualsFilterCriteria) {
      return createCondition((LowerThenEqualsFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof InFilterCriteria) {
      return createCondition((InFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof AndFilterCriteria) {
      return createCondition((AndFilterCriteria) filterCriteria, fromTable);
    } else if (filterCriteria instanceof GeometryFilterCriteria) {
      return createCondition((GeometryFilterCriteria) filterCriteria, fromTable);
    }

    throw unsupportedOperationException("Filter '{}' is not supported!", filterCriteria.getClass()
        .getName());
  }

  private Condition createCondition(AndFilterCriteria andFilterCriteria, String fromTable) {
    var innerConditions = andFilterCriteria.getFilterCriterias()
        .stream()
        .map(innerCriteria -> createCondition(innerCriteria, fromTable))
        .collect(Collectors.toList());

    return DSL.and(innerConditions);
  }

  private Condition createCondition(GeometryFilterCriteria geometryFilterCriteria, String fromTable) {
    Field<Object> field = getField(geometryFilterCriteria, fromTable);

    Field<?> geofilterField = getGeofilterField(geometryFilterCriteria);

    switch (geometryFilterCriteria.getFilterOperator()) {
      case CONTAINS:
        return DSL.condition("ST_Contains({0}, {1})", field, geofilterField);
      case WITHIN:
        return DSL.condition("ST_Within({0}, {1})", geofilterField, field);
      case INTERSECTS:
        return DSL.condition("ST_Intersects({0}, {1})", field, geofilterField);
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  private Condition createCondition(EqualsFilterCriteria equalsFilterCriteria, String fromTable) {
    Field<Object> field = getField(equalsFilterCriteria, fromTable);

    return field.eq(equalsFilterCriteria.getValue());
  }

  private Condition createCondition(GreaterThenFilterCriteria greaterThenFilterCriteria, String fromTable) {
    Field<Object> field = getField(greaterThenFilterCriteria, fromTable);

    return field.gt(greaterThenFilterCriteria.getValue());
  }

  private Condition createCondition(GreaterThenEqualsFilterCriteria greaterThenEqualsFilterCriteria, String fromTable) {
    Field<Object> field = getField(greaterThenEqualsFilterCriteria, fromTable);

    return field.ge(greaterThenEqualsFilterCriteria.getValue());
  }

  private Condition createCondition(LowerThenFilterCriteria lowerThenFilterCriteria, String fromTable) {
    Field<Object> field = getField(lowerThenFilterCriteria, fromTable);

    return field.lt(lowerThenFilterCriteria.getValue());
  }

  private Condition createCondition(LowerThenEqualsFilterCriteria lowerThenEqualsFilterCriteria, String fromTable) {
    Field<Object> field = getField(lowerThenEqualsFilterCriteria, fromTable);

    return field.le(lowerThenEqualsFilterCriteria.getValue());
  }

  private Condition createCondition(InFilterCriteria inFilterCriteria, String fromTable) {
    Field<Object> field = getField(inFilterCriteria, fromTable);

    return field.in(inFilterCriteria.getValues());
  }

  private Field<Object> getField(FilterCriteria filterCriteria, String fromTable) {
    var postgresFieldConfiguration = (PostgresFieldConfiguration) filterCriteria.getFieldPath()
        .getLeaf()
        .getFieldConfiguration();

    return DSL.field(DSL.name(fromTable, postgresFieldConfiguration.getColumn()));
  }

  private Field<?> getGeofilterField(GeometryFilterCriteria geometryFilterCriteria) {
    Geometry geometry = geometryFilterCriteria.getGeometry();

    if (geometry.getSRID() == 0) {
      geometry.setSRID(getSrid(geometryFilterCriteria.getCrs()));
    }

    return DSL.val(geometry)
        .cast(GEOMETRY_DATATYPE);
  }

  private Integer getSrid(String crs) {
    var srid = StringUtils.substringAfter(crs.toUpperCase(), SpatialConfigurationProperties.EPSG_PREFIX);

    return Integer.parseInt(srid);
  }

  private SelectQuery<?> buildQuery(FieldPath fieldPath, FilterCriteria filterCriteria, Table<?> table,
      ObjectSelectContext objectSelectContext, PostgresTypeConfiguration parentTypeConfiguration,
      List<ContextCriteria> contextCriterias) {
    var typeConfiguration = (PostgresTypeConfiguration) fieldPath.getFieldConfiguration()
        .getTypeConfiguration();

    var fromTable = findTable(typeConfiguration.getTable(), contextCriterias).as(objectSelectContext.newTableAlias());

    var query = dslContext.selectQuery(fromTable);

    query.addSelect(DSL.val(1));

    query.addConditions(createCondition(fieldPath.getChild(), filterCriteria, objectSelectContext, contextCriterias,
        typeConfiguration, fromTable));

    var lateralJoinContext = new ObjectSelectContext(objectSelectContext.getObjectQueryContext());

    var objectFieldConfiguration = (PostgresFieldConfiguration) fieldPath.getFieldConfiguration();

    Map<String, String> fieldAliasMap = lateralJoinContext.getFieldAliasMap();

    var leftSide = PostgresTableField.builder()
        .fieldConfiguration(objectFieldConfiguration)
        .table(fromTable)
        .build();

    var rightSide = PostgresTableType.builder()
        .typeConfiguration(parentTypeConfiguration)
        .table(table)
        .build();

    joinHelper.addJoinTableCondition(query, lateralJoinContext, leftSide, rightSide, fieldAliasMap, contextCriterias);

    List<Condition> joinConditions =
        joinHelper.createJoinConditions(objectFieldConfiguration, fromTable, table, fieldAliasMap);

    query.addConditions(joinConditions);

    return query;
  }
}
