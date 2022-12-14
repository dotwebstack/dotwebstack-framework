package org.dotwebstack.framework.backend.postgres.model;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.not;

import java.util.List;
import lombok.Data;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Data
public class GeometrySegmentsTable {
  private static final String TILE_ID = "tile_id";

  private final String schemaName;

  private final String tableName;

  private final String geoColumnName;

  private JoinColumn joinColumn;

  private List<JoinColumn> joinColumns;

  public GeometrySegmentsTable(String schemaName, String tableName, String geoColumnName,
      List<JoinColumn> joinColumns) {
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.geoColumnName = geoColumnName;
    this.joinColumns = joinColumns;
  }

  public Table<Record> getTable() {
    return DSL.table(DSL.name(schemaName, tableName))
        .as("gs");
  }

  public Field<Object> getTileIdField() {
    return DSL.field(DSL.name(getTable().getName(), TILE_ID));
  }

  public Field<Object> getGeomRdField() {
    return field(DSL.name(getTable().getName(), this.geoColumnName));
  }

  public Condition getTileIdCondition() {
    return TilesTable.getTileIdField()
        .equal(getTileIdField());
  }

  public Condition getIntersectsCondition() {
    return condition("ST_Intersects({0}, {1})", TilesTable.getGeomRdField(), getGeomRdField());
  }

  public Condition getWithinCondition() {
    return not(condition("ST_Within({0}, {1})", TilesTable.getGeomRdField(), getGeomRdField()));
  }

  public Condition getContainsCondition() {
    return not(condition("ST_Contains({0}, {1})", TilesTable.getGeomRdField(), getGeomRdField()));
  }

  public Condition getTouchesCondition() {
    return not(condition("ST_Touches({0}, {1})", TilesTable.getGeomRdField(), getGeomRdField()));
  }
}
