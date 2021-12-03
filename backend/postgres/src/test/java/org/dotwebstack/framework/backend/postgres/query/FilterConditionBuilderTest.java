package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.FilterConditionBuilder.newFiltering;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableBiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FilterConditionBuilderTest {

  private AliasManager aliasManager;

  private Table<Record> table;

  @BeforeEach
  void doBefore() {
    aliasManager = new AliasManager();
    table = DSL.table(DSL.name("x1"));
  }

  public static Stream<Arguments> getGeometryArguments() {
    return Stream.of(
        arguments(Map.of("contains", Map.of("fromWKT", "POINT(1 2)")),
            "(ST_Contains(\"x1\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"),
        arguments(Map.of("within", Map.of("fromWKT", "POINT(1 2)")),
            "(ST_Within(cast('POINT (1 2)' as geometry), \"x1\".\"geometry_column\"))"),
        arguments(Map.of("intersects", Map.of("fromWKT", "POINT(1 2)")),
            "(ST_Intersects(\"x1\".\"geometry_column\", cast('POINT (1 2)' as geometry)))"));
  }

  @ParameterizedTest
  @MethodSource("getGeometryArguments")
  void build_returnsConditions_forGeometryFilterCriterias(Map<String, Object> value, String expected) {
    var objectField = new PostgresObjectField();

    objectField.setType(SpatialConstants.GEOMETRY);

    var spatial = PostgresSpatial.builder()
        .srid(1)
        .spatialReferenceSystems(ImmutableBiMap.of(1, "geometry_column"))
        .build();

    objectField.setSpatial(spatial);

    var filterCriteria = FilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of(objectField))
        .value(value)
        .build();

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  public static Stream<Arguments> getBasicArguments() {
    return Stream.of(arguments(FilterType.EXACT, Map.of("eq", "foo"), "\"x1\".\"column\" = 'foo'"),
        arguments(FilterType.EXACT, Map.of("lt", "foo"), "\"x1\".\"column\" < 'foo'"),
        arguments(FilterType.EXACT, Map.of("lte", "foo"), "\"x1\".\"column\" <= 'foo'"),
        arguments(FilterType.EXACT, Map.of("gt", "foo"), "\"x1\".\"column\" > 'foo'"),
        arguments(FilterType.EXACT, Map.of("gte", "foo"), "\"x1\".\"column\" >= 'foo'"),
        arguments(FilterType.EXACT, Map.of("not", Map.of("eq", "foo")), "not (\"x1\".\"column\" = 'foo')"),
        arguments(FilterType.EXACT, Map.of("in", List.of("foo", "bar")),
            "\"x1\".\"column\" in (\n" + "  'foo', 'bar'\n" + ")"),
        arguments(FilterType.TERM, Map.of("eq", "foo"), "(\"x1\".\"tsv_column\" @@ plainto_tsquery('simple','foo'))"),
        arguments(FilterType.TERM, Map.of("not", Map.of("eq", "foo")),
            "not ((\"x1\".\"tsv_column\" @@ plainto_tsquery('simple','foo')))"));
  }

  @ParameterizedTest
  @MethodSource("getBasicArguments")
  void build_returnsConditions_forBasicFilterCriterias(FilterType filterType, Map<String, Object> values,
      String expected) {
    var filterCriteria = createFilterCriteria(filterType, values);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  public static Stream<Arguments> getUnknownArguments() {
    return Stream.of(arguments(FilterType.EXACT, Map.of("zz", "foo"), "Unknown filter field 'zz'"),
        arguments(FilterType.TERM, Map.of("zz", "foo"), "Unknown filter field 'zz'"));
  }

  @ParameterizedTest
  @MethodSource("getUnknownArguments")
  void build_throwsException_forUnknownFilterCriteria(FilterType filterType, Map<String, Object> values,
      String expected) {
    var filterCriteria = createFilterCriteria(filterType, values);

    var thrown = assertThrows(IllegalArgumentException.class, () -> build(filterCriteria));

    assertThat(thrown.getMessage(), equalTo(expected));
  }

  public static Stream<Arguments> getEnumArguments() {
    return Stream.of(arguments(Map.of("eq", "foo"), "\"x1\".\"column\" = cast('foo' as fooType)"),
        arguments(Map.of("lt", "foo"), "\"x1\".\"column\" < cast('foo' as fooType)"),
        arguments(Map.of("lte", "foo"), "\"x1\".\"column\" <= cast('foo' as fooType)"),
        arguments(Map.of("gt", "foo"), "\"x1\".\"column\" > cast('foo' as fooType)"),
        arguments(Map.of("gte", "foo"), "\"x1\".\"column\" >= cast('foo' as fooType)"),
        arguments(Map.of("not", Map.of("eq", "foo")), "not (\"x1\".\"column\" = cast('foo' as fooType))"),
        arguments(Map.of("in", List.of("foo", "bar")),
            "\"x1\".\"column\" in (\n  cast('foo' as fooType), cast('bar' as fooType)\n)"));
  }

  @ParameterizedTest
  @MethodSource("getEnumArguments")
  void build_returnsCondition_forEnumFilterCriteria(Map<String, Object> values, String expected) {
    var objectField = new PostgresObjectField();
    objectField.setColumn("column");

    var enumConfiguration = new FieldEnumConfiguration();
    enumConfiguration.setType("fooType");
    objectField.setEnumeration(enumConfiguration);

    var filterCriteria = createFilterCriteria(FilterType.EXACT, values, objectField);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  @Test
  void build_returnsCondition_forFieldPathWithMultipleItems() {
    var childObjectType = new PostgresObjectType();
    childObjectType.setTable("child_v");
    childObjectType.setFields(Map.of("child_id", new PostgresObjectField()));

    var joinColumns = new ArrayList<JoinColumn>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("parent_id");
    joinColumn.setReferencedColumn("child_id");
    joinColumns.add(joinColumn);

    var parentField = new PostgresObjectField();
    parentField.setJoinColumns(joinColumns);
    parentField.setTargetType(childObjectType);

    var childField = new PostgresObjectField();
    childField.setColumn("child_column");

    Map<String, Object> values = Map.of("eq", "foo");

    var filterCriteria = FilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of(parentField, childField))
        .value(values)
        .build();

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(),
        equalTo("exists (\n" + "  select 1\n" + "  from \"child_v\" \"x1\"\n" + "  where (\n"
            + "    \"x1\".\"parent_id\" = \"x1\".\"child_id\"\n" + "    and \"x1\".\"child_column\" = 'foo'\n" + "  )\n"
            + ")"));
  }

  private Condition build(FilterCriteria filterCriteria) {
    return newFiltering().filterCriteria(filterCriteria)
        .aliasManager(aliasManager)
        .table(table)
        .build();
  }

  private FilterCriteria createFilterCriteria(FilterType filterType, Map<String, Object> values) {
    var objectField = new PostgresObjectField();
    objectField.setColumn("column");
    objectField.setTsvColumn("tsv_column");

    return createFilterCriteria(filterType, values, objectField);
  }

  private FilterCriteria createFilterCriteria(FilterType filterType, Map<String, Object> values,
      PostgresObjectField objectField) {
    return FilterCriteria.builder()
        .filterType(filterType)
        .fieldPath(List.of(objectField))
        .value(values)
        .build();
  }
}
