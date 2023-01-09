package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.DefaultGeometryConditionBuilder.newDefaultGeometryConditionBuilder;
import static org.dotwebstack.framework.backend.postgres.query.SegmentsGeometryConditionBuilder.newSegmentsGeometryConditionBuilder;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.SRID;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TYPE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeometryConditionBuilderFactory {
  public static GeometryConditionBuilderBase getGeometryConditionBuilder(PostgresObjectField objectField,
      FilterOperator operator) {
    if (SRID == operator || TYPE == operator) {
      return newDefaultGeometryConditionBuilder().postgresObjectField(objectField)
          .filterOperator(operator);
    }
    if (objectField.getSpatial()
        .hasSegmentsTable()) {
      return newSegmentsGeometryConditionBuilder().postgresObjectField(objectField)
          .filterOperator(operator);
    }
    return newDefaultGeometryConditionBuilder().postgresObjectField(objectField)
        .filterOperator(operator);
  }
}
