package org.dotwebstack.framework.backend.postgres.model;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class TilesTable {
  private static final Table<Record> table = DSL.table(DSL.name("public", "tiles_10km"))
      .as("tls");

  private static final String TILE_ID = "tile_id";

  private static final String GEOM_RD = "geom_rd";

  public static Table<Record> getTable() {
    return table;
  }

  public static Field<Object> getTileIdField() {
    return DSL.field(DSL.name(table.getName(), TILE_ID));
  }

  public static Field<Object> getGeomRdField() {
    return DSL.field(DSL.name(table.getName(), GEOM_RD));
  }
}
