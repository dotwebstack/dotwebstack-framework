package org.dotwebstack.framework.backend.postgres.query;

import static graphql.Assert.assertTrue;
import static org.dotwebstack.framework.backend.postgres.query.DefaultGeometryConditionBuilder.newDefaultGeometryConditionBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableBiMap;
import java.util.Map;
import java.util.stream.Stream;
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

class DefaultGeometryConditionBuilderTest {

  @ParameterizedTest
  @MethodSource("getGeometryConditionBuilderArguments")
  void build_returnsCondition_forArguments(Object geometryValue, boolean unifyInputGeometry,
      FilterOperator filterOperator, String expectedCondition) {
    var objectField = mockObjectField(unifyInputGeometry);
    var srid = 1;
    var sourceTable = DSL.table(DSL.name("db", "brewery"))
        .as("src");

    var condition = newDefaultGeometryConditionBuilder().postgresObjectField(objectField)
        .filterOperator(filterOperator)
        .value(geometryValue)
        .srid(srid)
        .sourceTable(sourceTable)
        .build();

    assertThat(condition.isPresent(), is(true));
    assertThat(condition.get()
        .toString(), is(expectedCondition));
  }

  @Test
  void build_throwsException_forNotSupportedOperator() {
    var objectField = mockObjectField(false);
    var srid = 1;
    var sourceTable = DSL.table(DSL.name("db", "brewery"))
        .as("src");

    var defaultGeometryConditionBuilder = newDefaultGeometryConditionBuilder().postgresObjectField(objectField)
        .filterOperator(FilterOperator.EQ)
        .value(Map.of())
        .srid(srid)
        .sourceTable(sourceTable);

    Exception exception =
        Assertions.assertThrows(IllegalArgumentException.class, defaultGeometryConditionBuilder::build);

    String expectedMessage = "Unsupported filter operator 'EQ' for geometry filter operation";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  private static Stream<Arguments> getGeometryConditionBuilderArguments() {
    return Stream.of(
        arguments("POINT", false, FilterOperator.TYPE, "(GeometryType(\"src\".\"geometry_column\") = 'POINT')"),
        arguments(Map.of("fromWKT", "POINT(1 2)"), false, FilterOperator.CONTAINS,
            "(ST_Contains(\"src\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"),
        arguments(Map.of("fromWKT", "POINT(1 2)"), false, FilterOperator.WITHIN,
            "(ST_Within(\"src\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"),
        arguments(Map.of("fromWKT", "POINT(1 2)"), false, FilterOperator.INTERSECTS,
            "(ST_Intersects(\"src\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"),
        arguments(Map.of("fromWKT", "POINT(1 2)"), false, FilterOperator.TOUCHES,
            "(ST_Touches(\"src\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"),
        arguments(Map.of("fromWKT", "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)),((5 5, 15 5, 15 15, 5 15, 5 5)))"),
            false, FilterOperator.INTERSECTS,
            "(ST_Intersects(\"src\".\"geometry_column\", cast('MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)), "
                + "((5 5, 15 5, 15 15, 5 15, 5 5)))' as geometry)))"),
        arguments(Map.of("fromWKT", "MULTIPOLYGON (((0 0, 10 0, 10 10, 0 10, 0 0)),((5 5, 15 5, 15 15, 5 15, 5 5)))"),
            true, FilterOperator.INTERSECTS,
            "(ST_Intersects(\"src\".\"geometry_column\", cast('POLYGON ((10 5, 10 0, 0 0, 0 10, 5 10, "
                + "5 15, 15 15, 15 5, 10 5))' as geometry)))"));
  }

  private PostgresObjectField mockObjectField(boolean unifyInputGeometry) {
    var objectField = new PostgresObjectField();

    objectField.setType(SpatialConstants.GEOMETRY);

    var spatial = PostgresSpatial.builder()
        .srid(1)
        .spatialReferenceSystems(ImmutableBiMap.of(1, "geometry_column", 2, "geometry_column2"))
        .unifyInputGeometry(unifyInputGeometry)
        .build();

    objectField.setSpatial(spatial);
    return objectField;
  }


}
