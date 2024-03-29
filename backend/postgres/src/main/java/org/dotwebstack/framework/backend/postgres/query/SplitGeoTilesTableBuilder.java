package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;

import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.locationtech.jts.geom.Geometry;

@Setter
@Accessors(fluent = true)
public class SplitGeoTilesTableBuilder {
  private static final String GEOM_RD = "geom_rd";

  private static final String TILE_ID = "tile_id";

  private static final Table<Record> tilesTable = DSL.table(DSL.name("public", "tiles_10km"));

  private static final Field<Object> TILE_ID_FIELD = DSL.field(DSL.name(tilesTable.getName(), TILE_ID))
      .as(TILE_ID);

  private static final Field<Object> GEOM_RD_FIELD = DSL.field(DSL.name(tilesTable.getName(), GEOM_RD))
      .as(GEOM_RD);

  private final DSLContext dslContext = DSL.using(SQLDialect.POSTGRES);

  @NotNull
  private Field<Geometry> geometryValue;

  private SplitGeoTilesTableBuilder() {}

  static SplitGeoTilesTableBuilder newSplitGeoTilesTableBuilder() {
    return new SplitGeoTilesTableBuilder();
  }

  Table<Record> build() {
    validateFields(this);

    var intersectionField = createIntersectionField();
    var intersectsCondition = createIntersectionCondition();
    var splitGeoTilesQuery = dslContext.selectQuery(tilesTable);
    splitGeoTilesQuery.addSelect(TILE_ID_FIELD, intersectionField);
    splitGeoTilesQuery.addConditions(intersectsCondition);
    return splitGeoTilesQuery.asTable("tls");
  }

  private Field<byte[]> createIntersectionField() {
    return DSL.field("ST_Intersection({0}, {1})", byte[].class, GEOM_RD_FIELD, geometryValue)
        .as(GEOM_RD);
  }

  private Condition createIntersectionCondition() {
    return DSL.condition("ST_Intersects({0}, {1})", GEOM_RD_FIELD, geometryValue);
  }
}
