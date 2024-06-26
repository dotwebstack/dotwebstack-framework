package org.dotwebstack.framework.backend.postgres.query;

import static graphql.Assert.assertTrue;
import static org.dotwebstack.framework.backend.postgres.query.SegmentsGeometryConditionBuilder.newSegmentsGeometryConditionBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableBiMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.GeometrySegmentsTable;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SegmentsGeometryConditionBuilderTest {

  @Test
  void build_throwsException_forNotSupportedOperator() {
    var objectField = mockObjectField();
    var srid = 1;
    var sourceTable = DSL.table(DSL.name("db", "brewery"))
        .as("src");

    var segementGeometryCOnditionBuilder = newSegmentsGeometryConditionBuilder().postgresObjectField(objectField)
        .filterOperator(FilterOperator.EQ)
        .value(Map.of())
        .srid(srid)
        .sourceTable(sourceTable);

    Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      segementGeometryCOnditionBuilder.build();
    });

    String expectedMessage = "Unsupported filter operator 'EQ' for geometry filter operation";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @ParameterizedTest
  @MethodSource("getGeometryConditionBuilderArguments")
  void build_returnsCondition_forArguments(Object geometryValue, FilterOperator filterOperator,
      String expectedCondition) {
    var objectField = mockObjectField();
    var srid = 1;
    var sourceTable = DSL.table(DSL.name("db", "brewery"))
        .as("src");

    var condition = newSegmentsGeometryConditionBuilder().postgresObjectField(objectField)
        .filterOperator(filterOperator)
        .value(geometryValue)
        .srid(srid)
        .sourceTable(sourceTable)
        .build();

    assertThat(condition.isPresent(), is(true));
    assertThat(condition.get()
        .toString(), is(expectedCondition));
  }

  private static Stream<Arguments> getGeometryConditionBuilderArguments() {
    return Stream.of(
        arguments(Map.of("fromWKT", "POINT(1 2)"), FilterOperator.CONTAINS, getExpectedContainsCondition()),
        arguments(Map.of("fromWKT", "POINT(1 2)"), FilterOperator.WITHIN, getExpectedWithinCondition()),
        arguments(Map.of("fromWKT", "POINT(1 2)"), FilterOperator.INTERSECTS, getExpectedIntersectsCondition()),
        arguments(Map.of("fromWKT", "POINT(1 2)"), FilterOperator.TOUCHES, getExpectedTouchesCondition()));
  }

  private static String getExpectedContainsCondition() {
    return "not exists (\n" + "  select 1\n" + "  from \"dbeerpedia\".\"brewery__geometry__segments\" \"gs\"\n"
        + "    join (\n" + "      select\n" + "        \"tiles_10km\".\"tile_id\" \"tile_id\",\n"
        + "        ST_Intersection(\"geom_rd\", cast('POINT (1 2)' as geometry)) \"geom_rd\"\n"
        + "      from \"public\".\"tiles_10km\"\n"
        + "      where (ST_Intersects(\"geom_rd\", cast('POINT (1 2)' as geometry)))\n" + "    ) \"tls\"\n"
        + "      on (\n" + "        \"tls\".\"tile_id\" = \"gs\".\"tile_id\"\n"
        + "        and not (ST_Contains(\"tls\".\"geom_rd\", \"gs\".\"geometry\"))\n" + "      )\n"
        + "  where \"gs\".\"brewery__record_id\" = \"src\".\"record_id\"\n" + ")";
  }

  private static String getExpectedWithinCondition() {
    return "not exists (\n" + "  select 1\n" + "  from \"dbeerpedia\".\"brewery__geometry__segments\" \"gs\"\n"
        + "    join (\n" + "      select\n" + "        \"tiles_10km\".\"tile_id\" \"tile_id\",\n"
        + "        ST_Intersection(\"geom_rd\", cast('POINT (1 2)' as geometry)) \"geom_rd\"\n"
        + "      from \"public\".\"tiles_10km\"\n"
        + "      where (ST_Intersects(\"geom_rd\", cast('POINT (1 2)' as geometry)))\n" + "    ) \"tls\"\n"
        + "      on (\n" + "        \"tls\".\"tile_id\" = \"gs\".\"tile_id\"\n"
        + "        and not (ST_Within(\"tls\".\"geom_rd\", \"gs\".\"geometry\"))\n" + "      )\n"
        + "  where \"gs\".\"brewery__record_id\" = \"src\".\"record_id\"\n" + ")";
  }

  private static String getExpectedIntersectsCondition() {
    return "exists (\n" + "  select 1\n" + "  from \"dbeerpedia\".\"brewery__geometry__segments\" \"gs\"\n"
        + "    join (\n" + "      select\n" + "        \"tiles_10km\".\"tile_id\" \"tile_id\",\n"
        + "        ST_Intersection(\"geom_rd\", cast('POINT (1 2)' as geometry)) \"geom_rd\"\n"
        + "      from \"public\".\"tiles_10km\"\n"
        + "      where (ST_Intersects(\"geom_rd\", cast('POINT (1 2)' as geometry)))\n" + "    ) \"tls\"\n"
        + "      on (\n" + "        \"tls\".\"tile_id\" = \"gs\".\"tile_id\"\n"
        + "        and (ST_Intersects(\"tls\".\"geom_rd\", \"gs\".\"geometry\"))\n" + "      )\n"
        + "  where \"gs\".\"brewery__record_id\" = \"src\".\"record_id\"\n" + ")";
  }

  private static String getExpectedTouchesCondition() {
    return "not exists (\n" + "  select 1\n" + "  from \"dbeerpedia\".\"brewery__geometry__segments\" \"gs\"\n"
        + "    join (\n" + "      select\n" + "        \"tiles_10km\".\"tile_id\" \"tile_id\",\n"
        + "        ST_Intersection(\"geom_rd\", cast('POINT (1 2)' as geometry)) \"geom_rd\"\n"
        + "      from \"public\".\"tiles_10km\"\n"
        + "      where (ST_Intersects(\"geom_rd\", cast('POINT (1 2)' as geometry)))\n" + "    ) \"tls\"\n"
        + "      on (\n" + "        \"tls\".\"tile_id\" = \"gs\".\"tile_id\"\n"
        + "        and not (ST_Touches(\"tls\".\"geom_rd\", \"gs\".\"geometry\"))\n" + "      )\n"
        + "  where \"gs\".\"brewery__record_id\" = \"src\".\"record_id\"\n" + ")";
  }

  private PostgresObjectField mockObjectField() {
    var objectField = new PostgresObjectField();

    objectField.setType(SpatialConstants.GEOMETRY);
    var segmentsTable =
        new GeometrySegmentsTable("dbeerpedia", "brewery__geometry__segments", "geometry", List.of(createJoinColumn()));
    var spatial = PostgresSpatial.builder()
        .srid(1)
        .segmentsTable(Optional.of(segmentsTable))
        .spatialReferenceSystems(ImmutableBiMap.of(1, "geometry_column", 2, "geometry_column2"))
        .build();

    objectField.setSpatial(spatial);
    return objectField;
  }

  private JoinColumn createJoinColumn() {
    var joinColumn = new JoinColumn();
    joinColumn.setName("brewery__record_id");
    joinColumn.setReferencedColumn("record_id");
    return joinColumn;
  }
}
