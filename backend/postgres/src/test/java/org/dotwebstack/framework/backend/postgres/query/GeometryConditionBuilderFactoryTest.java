package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.GeometryConditionBuilderFactory.getGeometryConditionBuilder;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.CONTAINS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.INTERSECTS;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.SRID;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TOUCHES;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterOperator.WITHIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GeometryConditionBuilderFactoryTest {
  @ParameterizedTest
  @MethodSource("getFilterOperators")
  void getGeometryConditionBuilder_returnsSegmentsGeometryConditionBuilder(FilterOperator filterOperator,
      boolean hasSegmentsTable, Class expectedClazz) {
    var postgresObjectField = mockPostgresObjectField(hasSegmentsTable);
    var conditionBuilder = getGeometryConditionBuilder(postgresObjectField, filterOperator);

    assertThat(conditionBuilder, instanceOf(expectedClazz));
  }

  private static Stream<Arguments> getFilterOperators() {
    return Stream.of(arguments(TYPE, true, DefaultGeometryConditionBuilder.class),
        arguments(TYPE, false, DefaultGeometryConditionBuilder.class),
        arguments(SRID, true, DefaultGeometryConditionBuilder.class),
        arguments(SRID, false, DefaultGeometryConditionBuilder.class),
        arguments(CONTAINS, true, SegmentsGeometryConditionBuilder.class),
        arguments(CONTAINS, false, DefaultGeometryConditionBuilder.class),
        arguments(WITHIN, true, SegmentsGeometryConditionBuilder.class),
        arguments(WITHIN, false, DefaultGeometryConditionBuilder.class),
        arguments(INTERSECTS, true, SegmentsGeometryConditionBuilder.class),
        arguments(INTERSECTS, false, DefaultGeometryConditionBuilder.class),
        arguments(TOUCHES, true, SegmentsGeometryConditionBuilder.class),
        arguments(TOUCHES, false, DefaultGeometryConditionBuilder.class));
  }

  private PostgresObjectField mockPostgresObjectField(boolean hasSegmentsTable) {
    var postgresObjectField = new PostgresObjectField();
    var spatial = mock(PostgresSpatial.class);
    when(spatial.hasSegmentsTable()).thenReturn(hasSegmentsTable);
    postgresObjectField.setSpatial(spatial);
    return postgresObjectField;
  }
}
