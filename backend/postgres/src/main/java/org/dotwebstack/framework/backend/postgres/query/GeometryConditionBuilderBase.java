package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getColumnName;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSridOfColumnName;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.ext.spatial.GeometryReader.readGeometry;

import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.locationtech.jts.geom.Geometry;

@Accessors(fluent = true)
@Setter
public abstract class GeometryConditionBuilderBase {
  private static final DataType<Geometry> GEOMETRY_DATATYPE =
      new DefaultDataType<>(SQLDialect.POSTGRES, Geometry.class, "geometry");

  @NotNull
  protected PostgresObjectField postgresObjectField;

  @NotNull
  protected FilterOperator filterOperator;

  @NotNull
  protected Object value;

  protected Integer srid;

  @NotNull
  protected Table<Record> sourceTable;

  protected Field<Geometry> createGeometryFieldValue() {
    var columnName = getColumnName(postgresObjectField.getSpatial(), srid);
    return createGeometryFieldValue(columnName);
  }

  protected Field<Geometry> createGeometryFieldValue(String columnName) {
    var mapValue = ObjectHelper.castToMap(value);

    var geometry = readGeometry(mapValue);
    var columnSrid = getSridOfColumnName(postgresObjectField.getSpatial(), columnName);
    geometry.setSRID(columnSrid);

    return DSL.val(geometry)
        .cast(GEOMETRY_DATATYPE);
  }

  protected void validateSupportedOperators(FilterOperator filterOperator) {
    if (!getSupportedOperators().contains(filterOperator)) {
      throw illegalArgumentException("Unsupported segment geometry filter operation");
    }
  }

  abstract Set<FilterOperator> getSupportedOperators();

  abstract Optional<Condition> build();
}
