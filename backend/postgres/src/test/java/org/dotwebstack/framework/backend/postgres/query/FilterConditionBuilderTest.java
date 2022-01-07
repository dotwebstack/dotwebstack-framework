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
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.backend.filter.GroupFilterOperator;
import org.dotwebstack.framework.core.backend.filter.ScalarFieldFilterCriteria;
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
        arguments(Map.of("contains", Map.of("fromWKT", "POINT(1 2)"), "srid", 2),
            "(ST_Contains(\"x1\".\"geometry_column2\", cast('POINT (1 2)' as geometry)))"),
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
        .spatialReferenceSystems(ImmutableBiMap.of(1, "geometry_column", 2, "geometry_column2"))
        .build();

    objectField.setSpatial(spatial);

    var filterCriteria = ScalarFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of(objectField))
        .value(value)
        .build();

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  public static Stream<Arguments> getScalarFieldArguments() {
    return Stream.of(arguments(FilterType.EXACT, Map.of("eq", "foo"), "\"x1\".\"column\" = 'foo'"),
        arguments(FilterType.EXACT, Map.of("lt", "foo"), "\"x1\".\"column\" < 'foo'"),
        arguments(FilterType.EXACT, Map.of("lte", "foo"), "\"x1\".\"column\" <= 'foo'"),
        arguments(FilterType.EXACT, Map.of("gt", "foo"), "\"x1\".\"column\" > 'foo'"),
        arguments(FilterType.EXACT, Map.of("gte", "foo"), "\"x1\".\"column\" >= 'foo'"),
        arguments(FilterType.EXACT, Map.of("not", Map.of("eq", "foo")), "not (\"x1\".\"column\" = 'foo')"),
        arguments(FilterType.EXACT, Map.of("in", List.of("foo", "bar")),
            "\"x1\".\"column\" in (\n" + "  'foo', 'bar'\n" + ")"),
        arguments(FilterType.PARTIAL, Map.of("match", "foo"),
            "lower(\"x1\".\"column\") like lower('%foo%') escape '\\'"),
        arguments(FilterType.PARTIAL, Map.of("not", Map.of("match", "foo")),
            "not (lower(\"x1\".\"column\") like lower('%foo%') escape '\\')"));
  }

  @ParameterizedTest
  @MethodSource("getScalarFieldArguments")
  void build_returnsConditions_forScalarFieldFilterCriterias(FilterType filterType, Map<String, Object> values,
      String expected) {
    var filterCriteria = createScalarFieldFilterCriteria(filterType, values);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  public static Stream<Arguments> getCaseInsensitiveScalarFieldArguments() {
    return Stream.of(
        arguments(FilterType.EXACT, Map.of("eq", "foo"), "lower(cast(\"x1\".\"column\" as varchar)) = lower('foo')"),
        arguments(FilterType.EXACT, Map.of("not", Map.of("eq", "foo")),
            "not (lower(cast(\"x1\".\"column\" as varchar)) = lower('foo'))"),
        arguments(FilterType.EXACT, Map.of("in", List.of("foo", "bar")),
            "lower(\"x1\".\"column\") in (\n" + "  'foo', 'bar'\n" + ")"));
  }

  @ParameterizedTest
  @MethodSource("getCaseInsensitiveScalarFieldArguments")
  void build_returnsConditions_forCaseInsensitiveFilterCriterias(FilterType filterType, Map<String, Object> values,
      String expected) {
    var filterCriteria = createScalarFieldFilterCriteria(filterType, values, "String", false);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  public static Stream<Arguments> getUnknownArguments() {
    return Stream.of(arguments(FilterType.EXACT, Map.of("zz", "foo"), "Unknown filter field 'zz' for type 'String'"),
        arguments(FilterType.PARTIAL, Map.of("zz", "foo"), "Unknown filter field 'zz' for type 'String'"));
  }

  @ParameterizedTest
  @MethodSource("getUnknownArguments")
  void build_throwsException_forUnknownFilterCriteria(FilterType filterType, Map<String, Object> values,
      String expected) {
    var filterCriteria = createScalarFieldFilterCriteria(filterType, values);

    var thrown = assertThrows(IllegalArgumentException.class, () -> build(filterCriteria));

    assertThat(thrown.getMessage(), equalTo(expected));
  }

  private static Stream<Arguments> getListArguments() {
    return Stream.of(arguments("String", Map.of("eq", List.of("foo")), "\"x1\".\"column\" = array['foo']"),
        arguments("String", Map.of("containsAllOf", List.of("foo")), "\"x1\".\"column\" @> array['foo']"),
        arguments("String", Map.of("containsAnyOf", List.of("foo")), "(\"x1\".\"column\" && array['foo'])"),
        arguments("String", Map.of("not", Map.of("containsAnyOf", List.of("foo"))),
            "not ((\"x1\".\"column\" && array['foo']))"),
        arguments("Int", Map.of("eq", List.of(33, 44)), "\"x1\".\"column\" = array[33, 44]"),
        arguments("Int", Map.of("containsAllOf", List.of(33, 44)), "\"x1\".\"column\" @> array[33, 44]"),
        arguments("Int", Map.of("containsAnyOf", List.of(33, 44)), "(\"x1\".\"column\" && array[33, 44])"),
        arguments("Int", Map.of("not", Map.of("containsAnyOf", List.of(33, 44))),
            "not ((\"x1\".\"column\" && array[33, 44]))"),
        arguments("Float", Map.of("eq", List.of(33.3f, 44.4f)), "\"x1\".\"column\" = array[33.3, 44.4]"),
        arguments("Float", Map.of("containsAllOf", List.of(33.3f, 44.4f)), "\"x1\".\"column\" @> array[33.3, 44.4]"),
        arguments("Float", Map.of("containsAnyOf", List.of(33.3f, 44.4f)), "(\"x1\".\"column\" && array[33.3, 44.4])"),
        arguments("Float", Map.of("not", Map.of("containsAnyOf", List.of(33.3f, 44.4f))),
            "not ((\"x1\".\"column\" && array[33.3, 44.4]))"));
  }

  @ParameterizedTest
  @MethodSource("getListArguments")
  void build_returnsCondition_forListFilterCriteria(String type, Map<String, Object> values, String expected) {
    var objectField = new PostgresObjectField();
    objectField.setColumn("column");
    objectField.setList(true);
    objectField.setType(type);

    var filterCriteria = createScalarFieldFilterCriteria(FilterType.EXACT, values, objectField, true);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  private static Stream<Arguments> getEnumListArguments() {
    return Stream.of(arguments(Map.of("eq", List.of("foo")), "\"x1\".\"column\" = cast(array['foo'] as fooType[])"),
        arguments(Map.of("containsAllOf", List.of("foo")), "\"x1\".\"column\" @> cast(array['foo'] as fooType[])"),
        arguments(Map.of("containsAnyOf", List.of("foo")), "(\"x1\".\"column\" && cast(array['foo'] as fooType[]))"),
        arguments(Map.of("not", Map.of("containsAnyOf", List.of("foo"))),
            "not ((\"x1\".\"column\" && cast(array['foo'] as fooType[])))"));
  }

  @ParameterizedTest
  @MethodSource("getEnumListArguments")
  void build_returnsCondition_forEnumListFilterCriteria(Map<String, Object> values, String expected) {
    var objectField = new PostgresObjectField();
    objectField.setColumn("column");
    objectField.setList(true);
    objectField.setType("String");

    var enumConfiguration = new FieldEnumConfiguration();
    enumConfiguration.setType("fooType");
    objectField.setEnumeration(enumConfiguration);

    var filterCriteria = createScalarFieldFilterCriteria(FilterType.EXACT, values, objectField, true);

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo(expected));
  }

  private static Stream<Arguments> getEnumArguments() {
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

    var filterCriteria = createScalarFieldFilterCriteria(FilterType.EXACT, values, objectField, true);

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
    parentField.setObjectType(new PostgresObjectType());
    parentField.setTargetType(childObjectType);

    var childField = new PostgresObjectField();
    childField.setColumn("child_column");

    Map<String, Object> values = Map.of("eq", "foo");

    var filterCriteria = ScalarFieldFilterCriteria.builder()
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

  @Test
  void build_returnsCondition_forReferenceObjectWithJoinColumn() {
    var identifierField = new PostgresObjectField();
    identifierField.setName("identifier");

    var refObjectType = new PostgresObjectType();
    refObjectType.setFields(Map.of("identifier", identifierField));

    var refField = new PostgresObjectField();
    refField.setName("ref");
    refField.setTargetType(refObjectType);

    var childObjectType = new PostgresObjectType();
    childObjectType.setFields(Map.of("ref", refField));

    var joinColumns = new ArrayList<JoinColumn>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("parent_id");
    joinColumn.setReferencedField("ref.identifier");
    joinColumns.add(joinColumn);

    var childField = new PostgresObjectField();
    childField.setName("child");
    childField.setJoinColumns(joinColumns);
    childField.setTargetType(childObjectType);

    Map<String, Object> values = Map.of("eq", "123");

    var filterCriteria = ScalarFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of(childField, refField, identifierField))
        .value(values)
        .build();

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo("\"x1\".\"parent_id\" = '123'"));
  }

  @Test
  void build_returnsCondition_forReferenceObjectWithJoinTable() {
    var identifierField = new PostgresObjectField();
    identifierField.setName("identifier");

    var refObjectType = new PostgresObjectType();
    refObjectType.setFields(Map.of("identifier", identifierField));

    var refsField = new PostgresObjectField();
    refsField.setName("refs");
    refsField.setTargetType(refObjectType);

    var childObjectType = new PostgresObjectType();
    childObjectType.setFields(Map.of("refs", refsField));

    var joinTable = new JoinTable();
    joinTable.setName("parent_child");
    var joinColumns = new ArrayList<JoinColumn>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("parent__id");
    joinColumn.setReferencedField("parent_id");
    joinColumns.add(joinColumn);

    var inverseJoinColumns = new ArrayList<JoinColumn>();
    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("child__id");
    inverseJoinColumn.setReferencedField("refs.identifier");
    inverseJoinColumns.add(inverseJoinColumn);

    joinTable.setJoinColumns(joinColumns);
    joinTable.setInverseJoinColumns(inverseJoinColumns);

    var childField = new PostgresObjectField();
    childField.setColumn("child");
    childField.setJoinTable(joinTable);
    childField.setTargetType(childObjectType);

    var parentIdentifierField = new PostgresObjectField();
    parentIdentifierField.setColumn("parent_id");

    var parentType = new PostgresObjectType();
    parentType.setFields(Map.of("parent_id", parentIdentifierField, "child", childField));
    childField.setObjectType(parentType);

    Map<String, Object> values = Map.of("eq", "123");

    var filterCriteria = ScalarFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of(childField, refsField, identifierField))
        .value(values)
        .build();

    var condition = build(filterCriteria);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(),
        equalTo("exists (\n" + "  select 1\n" + "  from \"parent_child\"\n" + "  where (\n"
            + "    \"parent_child\".\"parent__id\" = \"x1\".\"parent_id\"\n"
            + "    and \"parent_child\".\"child__id\" = '123'\n" + "  )\n" + ")"));
  }

  @Test
  void build_returnsCondition_forGroupFilterCriteria() {
    var childAndGroup1 = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of(createScalarFieldFilterCriteria(FilterType.EXACT, Map.of("eq", "foo")),
            createScalarFieldFilterCriteria(FilterType.EXACT, Map.of("eq", "bar"))))
        .build();

    var childAndGroup2 = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of(createScalarFieldFilterCriteria(FilterType.EXACT, Map.of("eq", "foobar"))))
        .build();

    var orGroup = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.OR)
        .filterCriterias(List.of(childAndGroup1, childAndGroup2))
        .build();

    var condition = build(orGroup);

    assertThat(condition, notNullValue());
    assertThat(condition.toString(), equalTo("(\n" + "  (\n" + "    \"x1\".\"column\" = 'foo'\n"
        + "    and \"x1\".\"column\" = 'bar'\n" + "  )\n" + "  or \"x1\".\"column\" = 'foobar'\n" + ")"));
  }

  private Condition build(FilterCriteria filterCriteria) {
    return newFiltering().filterCriteria(filterCriteria)
        .aliasManager(aliasManager)
        .table(table)
        .build();
  }

  private FilterCriteria createScalarFieldFilterCriteria(FilterType filterType, Map<String, Object> values) {
    return createScalarFieldFilterCriteria(filterType, values, "String", true);
  }

  private FilterCriteria createScalarFieldFilterCriteria(FilterType filterType, Map<String, Object> values, String type,
      boolean caseSensitive) {
    var objectField = new PostgresObjectField();
    objectField.setColumn("column");
    objectField.setType(type);

    return createScalarFieldFilterCriteria(filterType, values, objectField, caseSensitive);
  }

  private FilterCriteria createScalarFieldFilterCriteria(FilterType filterType, Map<String, Object> values,
      PostgresObjectField objectField, boolean caseSensitive) {
    return ScalarFieldFilterCriteria.builder()
        .filterType(filterType)
        .isCaseSensitive(caseSensitive)
        .fieldPath(List.of(objectField))
        .value(values)
        .build();
  }
}
