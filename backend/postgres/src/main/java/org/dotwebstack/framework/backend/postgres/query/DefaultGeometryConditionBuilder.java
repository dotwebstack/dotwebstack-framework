package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.INTERSECTS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.SRID;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TOUCHES;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.WITHIN;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Optional;
import java.util.Set;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.jooq.Condition;
import org.jooq.impl.DSL;

@Accessors(fluent = true)
@Setter
public class DefaultGeometryConditionBuilder extends GeometryConditionBuilderBase {
  private static final Set<FilterOperator> SUPPORTED_OPERATORS =
      Set.of(SRID, TYPE, INTERSECTS, CONTAINS, WITHIN, TOUCHES);

  private DefaultGeometryConditionBuilder() {}

  static DefaultGeometryConditionBuilder newDefaultGeometryConditionBuilder() {
    return new DefaultGeometryConditionBuilder();
  }

  Optional<Condition> build() {
    validateFields(this);
    validateSupportedOperators(filterOperator);

    if (SRID == filterOperator) {
      return Optional.empty();
    }

    var columnName = getColumnName(postgresObjectField.getSpatial(), srid);
    var field = DSL.field(DSL.name(sourceTable.getName(), columnName));

    if (TYPE == filterOperator) {
      return Optional.of(DSL.condition("GeometryType({0}) = {1}", field, value));
    }

    var geoFieldValue = createGeometryFieldValue(columnName);

    switch (filterOperator) {
      case CONTAINS:
        return Optional.of(DSL.condition("ST_Contains({0}, {1})", field, geoFieldValue));
      case WITHIN:
        return Optional.of(DSL.condition("ST_Within({0}, {1})", geoFieldValue, field));
      case INTERSECTS:
        return Optional.of(DSL.condition("ST_Intersects({0}, {1})", field, geoFieldValue));
      case TOUCHES:
        return Optional.of(DSL.condition("ST_Touches({0}, {1})", field, geoFieldValue));
      default:
        throw illegalArgumentException("Unsupported geometry filter operation");
    }
  }

  protected Set<FilterOperator> getSupportedOperators() {
    return SUPPORTED_OPERATORS;
  }
}
