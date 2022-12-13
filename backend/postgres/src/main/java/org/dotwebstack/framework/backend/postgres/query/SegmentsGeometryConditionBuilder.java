package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.backend.postgres.query.SplitGeoTilesTableBuilder.newSplitGeoTilesTableBuilder;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.INTERSECTS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TOUCHES;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.WITHIN;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.jooq.JoinType.JOIN;
import static org.jooq.impl.DSL.not;

import java.util.Optional;
import java.util.Set;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.GeometrySegmentsTable;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Accessors(fluent = true)
@Setter
public class SegmentsGeometryConditionBuilder extends GeometryConditionBuilderBase {
  private static final Set<FilterOperator> SUPPORTED_OPERATORS = Set.of(INTERSECTS, CONTAINS, WITHIN, TOUCHES);

  private static final String INFIX = "__";

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  private SegmentsGeometryConditionBuilder() {}

  static SegmentsGeometryConditionBuilder newSegmentsGeometryConditionBuilder() {
    return new SegmentsGeometryConditionBuilder();
  }

  Optional<Condition> build() {
    validateFields(this);
    validateSupportedOperators(filterOperator);
    return buildCondition();
  }

  private Optional<Condition> buildCondition() {
    var geometryCondition = createGeometryCondition();
    return Optional.of(geometryCondition);
  }

  private void validateSupportedOperators(FilterOperator filterOperator) {
    if (!SUPPORTED_OPERATORS.contains(filterOperator)) {
      throw illegalArgumentException("Unsupported segment geometry filter operation");
    }
  }

  private Condition createGeometryCondition() {
    var segmentsTable = postgresObjectField.getSpatial()
        .getSegmentsTable()
        .get();
    var segmentsTableAlias = segmentsTable.getTable();
    var splitGeoTileTable = createSplitGeoTilesTable();

    var joinCondition = segmentsTable.getTileIdCondition();
    var operatorCondition = createOperatorCondition(segmentsTable);

    var segmentsQuery = dslContext.selectQuery(segmentsTableAlias);
    segmentsQuery.addSelect(DSL.val(1));
    segmentsQuery.addJoin(splitGeoTileTable, JOIN, joinCondition, operatorCondition);

    var whereCondition = createWhereCondition(segmentsTable);
    segmentsQuery.addConditions(whereCondition);

    var geometryCondition = DSL.exists(segmentsQuery);
    if (filterOperator != INTERSECTS) {
      geometryCondition = not(geometryCondition);
    }
    return geometryCondition;
  }

  private Condition createOperatorCondition(GeometrySegmentsTable geometrySegmentsTable) {
    switch (filterOperator) {
      case CONTAINS:
        return geometrySegmentsTable.getContainsCondition();
      case WITHIN:
        return geometrySegmentsTable.getWithinCondition();
      case INTERSECTS:
        return geometrySegmentsTable.getIntersectsCondition();
      case TOUCHES:
        return geometrySegmentsTable.getTouchesCondition();
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  private Condition createWhereCondition(final GeometrySegmentsTable segmentsTable) {
    var joinColumn = segmentsTable.getJoinColumn();
    return column(sourceTable, joinColumn.getReferencedColumn())
        .equal(column(segmentsTable.getTable(), joinColumn.getName()));
  }

  private Table<Record> createSplitGeoTilesTable() {
    var geometryValue = createGeometryFieldValue();
    return newSplitGeoTilesTableBuilder().geometryValue(geometryValue)
        .build();
  }
}
