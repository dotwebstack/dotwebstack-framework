package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.FilterConditionHelper.createFilterCondition;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
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
import org.jooq.Condition;
import org.jooq.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterConditionHelperTest {

  @Mock
  private Table<?> fromTable;

  @Mock
  private ObjectSelectContext objectSelectContext;

  @Mock
  private PostgresFieldConfiguration fieldConfiguration;

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
  }

  @Test
  void createFilterConditions_returnConditions_forEqualsCriteria() {
    var filterCriteria = EqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value("testValue")
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("\"t1\".\"test_column\" = 'testValue'"));
  }

  @Test
  void createFilterConditions_returnConditions_forNotCriteria() {
    var filterCriteria = NotFilterCriteria.builder()
        .filterCriteria(EqualsFilterCriteria.builder()
            .fieldPath(fieldPath)
            .value("testValue")
            .build())
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("not (\"t1\".\"test_column\" = 'testValue')"));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = ";",
      value = {"CONTAINS;(ST_Contains(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)')))",
          "WITHIN;(ST_Within(ST_GeomFromText('POINT (1 1)'), \"t1\".\"test_column\"))",
          "INTERSECTS;(ST_Intersects(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)')))"})
  void createFilterConditions_returnConditions_forGeometryCriteria(String filterOperator, String expected)
      throws ParseException {
    var wkt = "POINT (1 1)";

    var wktReader = new WKTReader();

    var filterCriteria = GeometryFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterOperator(GeometryFilterOperator.valueOf(filterOperator))
        .geometry(wktReader.read(wkt))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(expected));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = ";",
      value = {"CONTAINS;(ST_Contains(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)',4258)))",
          "WITHIN;(ST_Within(ST_GeomFromText('POINT (1 1)',4258), \"t1\".\"test_column\"))",
          "INTERSECTS;(ST_Intersects(\"t1\".\"test_column\", ST_GeomFromText('POINT (1 1)',4258)))"})
  void createFilterConditions_returnConditions_forGeometryCriteriaWithCrs(String filterOperator, String expected)
      throws ParseException {
    var wkt = "POINT (1 1)";

    var wktReader = new WKTReader();

    var filterCriteria = GeometryFilterCriteria.builder()
        .fieldPath(fieldPath)
        .filterOperator(GeometryFilterOperator.valueOf(filterOperator))
        .geometry(wktReader.read(wkt))
        .crs("EPSG:4258")
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo(expected));
  }

  @Test
  void createFilterConditions_returnConditions_forAndCriteria() {
    var filterCriteria = AndFilterCriteria.builder()
        .filterCriterias(List.of(EqualsFilterCriteria.builder()
            .fieldPath(fieldPath)
            .value("testValue1")
            .build(),
            EqualsFilterCriteria.builder()
                .fieldPath(fieldPath)
                .value("testValue2")
                .build()))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("(\n  \"t1\".\"test_column\" = 'testValue1'\n  and \"t1\".\"test_column\" = 'testValue2'\n)"));
  }

  @Test
  void createFilterConditions_returnConditions_forInCriteria() {
    var filterCriteria = InFilterCriteria.builder()
        .fieldPath(fieldPath)
        .values(List.of("testValue1", "testValue2"))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("\"t1\".\"test_column\" in (\n  'testValue1', 'testValue2'\n)"));
  }

  @Test
  void createFilterConditions_returnConditions_forGreaterThenFilterCriteria() {
    var filterCriteria = GreaterThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" > timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createFilterConditions_returnConditions_forGreaterThenEqualsFilterCriteria() {
    var filterCriteria = GreaterThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" >= timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createFilterConditions_returnConditions_forLowerThenFilterCriteria() {
    var filterCriteria = LowerThenFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" < timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createFilterConditions_returnConditions_forLowerThenEqualsFilterCriteria() {
    var filterCriteria = LowerThenEqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    Condition result = createFilterCondition(filterCriteria, fromTable.getName());

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("\"t1\".\"test_column\" <= timestamp with time zone '2020-10-01 11:00:05+00:00'"));
  }

  @Test
  void createFilterConditions_throwsException_forUnsupportedCriteria() {
    var filterCriteria = new FilterCriteria() {

      @Override
      public List<FieldPath> getFieldPaths() {
        return null;
      }
    };

    String tableName = fromTable.getName();

    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> createFilterCondition(filterCriteria, tableName));

  }

}
