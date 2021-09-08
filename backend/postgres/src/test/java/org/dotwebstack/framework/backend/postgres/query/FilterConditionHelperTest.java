package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.dotwebstack.framework.ext.spatial.GeometryFilterCriteria;
import org.dotwebstack.framework.ext.spatial.GeometryFilterOperator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class FilterConditionHelperTest {

  @Mock
  private Table<?> fromTable;

  @Mock
  private ObjectSelectContext objectSelectContext;

  @Mock
  private ObjectRequest objectRequest;

  @Mock
  private PostgresFieldConfiguration fieldConfiguration;

  private DSLContext dslContext = new DefaultDSLContext(SQLDialect.POSTGRES);

  @Mock
  private JoinHelper joinHelper;

  private FilterConditionHelper filterConditionHelper;

  private FieldPath fieldPath;

  @BeforeEach
  public void doBefore() {
    lenient().when(fromTable.getName())
        .thenReturn("t1");
    lenient().when(objectSelectContext.getTableAlias(anyString()))
        .thenReturn("t1");
    lenient().when(fieldConfiguration.getColumn())
        .thenReturn("test_column");

    fieldPath = FieldPath.builder()
        .fieldConfiguration(fieldConfiguration)
        .build();

    filterConditionHelper = new FilterConditionHelper(dslContext, joinHelper);
  }

  @Test
  void createCondition_returnsValue_forEqualsCriteria() {
    var filterCriteria = EqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value("testValue")
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("\"t1\".\"test_column\" = 'testValue'"));
  }

  @Test
  void createCondition_returnsValue_forNotCriteria() {
    var filterCriteria = NotFilterCriteria.builder()
        .filterCriteria(EqualsFilterCriteria.builder()
            .fieldPath(fieldPath)
            .value("testValue")
            .build())
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("not (\"t1\".\"test_column\" = 'testValue')"));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = ";",
      value = {"CONTAINS;(ST_Contains(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)')))",
          "WITHIN;(ST_Within(ST_GeomFromText('POINT (1 1)'), \"t1\".\"test_column\"))",
          "INTERSECTS;(ST_Intersects(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)')))"})
  void createCondition_returnsValue_forGeometryCriteria(String filterOperator, String expected) throws ParseException {
    var wkt = "POINT (1 1)";

    var wktReader = new WKTReader();

    var filterCriteria = GeometryFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterOperator(GeometryFilterOperator.valueOf(filterOperator))
        .geometry(wktReader.read(wkt))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(expected));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = ";",
      value = {"CONTAINS;(ST_Contains(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)',4258)))",
          "WITHIN;(ST_Within(ST_GeomFromText('POINT (1 1)',4258), \"t1\".\"test_column\"))",
          "INTERSECTS;(ST_Intersects(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)',4258)))"})
  void createCondition_returnsValue_forGeometryCriteriaWithCrs(String filterOperator, String expected)
      throws ParseException {
    var wkt = "POINT (1 1)";

    var wktReader = new WKTReader();

    var filterCriteria = GeometryFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterOperator(GeometryFilterOperator.valueOf(filterOperator))
        .geometry(wktReader.read(wkt))
        .crs("EPSG:4258")
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(expected));
  }

  @Test
  void createCondition_returnsValue_forAndCriteria() {
    var filterCriteria = AndFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterCriterias(List.of(EqualsFilterCriteria.builder()
            .fieldPath(fieldPath)
            .value("testValue1")
            .build(),
            EqualsFilterCriteria.builder()
                .fieldPath(fieldPath)
                .value("testValue2")
                .build()))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(
        "(\n" + "  \"t1\".\"test_column\" = 'testValue1'\n" + "  and \"t1\".\"test_column\" = 'testValue2'\n" + ")"));
  }

  @Test
  void createCondition_returnsValue_forInCriteria() {
    var filterCriteria = InFilterCriteria.builder()
        .fieldPath(fieldPath)
        .values(List.of("testValue1", "testValue2"))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("\"t1\".\"test_column\" in (\n" + "  'testValue1', 'testValue2'\n" + ")"));
  }

  @Test
  void createCondition_returnsValue_forGreaterThenFilterCriteria() {
    var filterCriteria = GreaterThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" > timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createCondition_returnsValue_forGreaterThenEqualsFilterCriteria() {
    var filterCriteria = GreaterThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" >= timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createCondition_returnsValue_forLowerThenFilterCriteria() {
    var filterCriteria = LowerThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" < timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createCondition_returnsValue_forLowerThenEqualsFilterCriteria() {
    var filterCriteria = LowerThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" <= timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  @SuppressWarnings({"raw", "unchecked"})
  void createCondition_returnsValue_forNestedFilterCriteria() {
    when(joinHelper.createJoinConditions(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
        ArgumentMatchers.anyMap())).thenReturn(List.of(DSL.condition("1 = 1")));

    when(objectSelectContext.newTableAlias()).thenAnswer(new Answer<String>() {

      private final AtomicInteger counter = new AtomicInteger(1);

      @Override
      public String answer(InvocationOnMock invocationOnMock) {
        return String.format("t%s", counter.getAndIncrement());
      }
    });

    var typeConfigurationNode1 = mock(PostgresTypeConfiguration.class);
    when(typeConfigurationNode1.getTable()).thenReturn("node1");

    var fieldConfigurationNode1 = mock(PostgresFieldConfiguration.class);

    when(fieldConfigurationNode1.getTypeConfiguration()).thenReturn((TypeConfiguration) typeConfigurationNode1);

    var typeConfigurationNode2 = mock(PostgresTypeConfiguration.class);
    when(typeConfigurationNode2.getTable()).thenReturn("node2");

    var fieldConfigurationNode2 = mock(PostgresFieldConfiguration.class);
    when(fieldConfigurationNode2.getTypeConfiguration()).thenReturn((TypeConfiguration) typeConfigurationNode2);

    var fieldConfigurationLeaf = mock(PostgresFieldConfiguration.class);

    fieldPath = FieldPath.builder()
        .fieldConfiguration(fieldConfigurationNode1)
        .child(FieldPath.builder()
            .fieldConfiguration(fieldConfigurationNode2)
            .child(FieldPath.builder()
                .fieldConfiguration(fieldConfigurationLeaf)
                .build())
            .build())
        .build();

    var filterCriteria = EqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value("testValue")
        .build();

    var result = filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("exists (\n" + "  select 1\n" + "  from \"node1\" \"t1\"\n" + "  where (\n" + "    exists (\n"
            + "      select 1\n" + "      from \"node2\" \"t2\"\n" + "      where (\n"
            + "        \"t2\" = 'testValue'\n" + "        and (1 = 1)\n" + "      )\n" + "    )\n" + "    and (1 = 1)\n"
            + "  )\n" + ")"));
  }

  @Test
  void createCondition_throwsException_forUnsupportedCriteria() {
    var filterCriteria = new FilterCriteria() {

      @Override
      public FieldPath getFieldPath() {
        return fieldPath;
      }
    };

    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> filterConditionHelper.createCondition(filterCriteria, fromTable, objectSelectContext, objectRequest));

  }

}
