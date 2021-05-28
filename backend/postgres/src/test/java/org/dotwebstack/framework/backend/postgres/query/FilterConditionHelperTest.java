package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.FilterConditionHelper.createFilterConditions;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.jooq.Condition;
import org.jooq.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class FilterConditionHelperTest {

  @Mock
  private Table<?> fromTable;

  @Mock
  private PostgresFieldConfiguration fieldConfiguration;

  @BeforeEach
  public void doBefore() {
    lenient().when(fromTable.getName())
        .thenReturn("t1");
    lenient().when(fieldConfiguration.getColumn())
        .thenReturn("test_column");
  }

  @Test
  void createFilterConditions_returnConditions_forEqualsCriteria() {
    var filterCriteria = EqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value("testValue")
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("[\"t1\".\"test_column\" = 'testValue']"));
  }

  @Test
  void createFilterConditions_returnConditions_forNotCriteria() {
    var filterCriteria = NotFilterCriteria.builder()
        .filterCriteria(EqualsFilterCriteria.builder()
            .field(fieldConfiguration)
            .value("testValue")
            .build())
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("[not (\"t1\".\"test_column\" = 'testValue')]"));
  }

  @Test
  void createFilterConditions_returnConditions_forAndCriteria() {
    var filterCriteria = AndFilterCriteria.builder()
        .filterCriterias(List.of(EqualsFilterCriteria.builder()
            .field(fieldConfiguration)
            .value("testValue1")
            .build(),
            EqualsFilterCriteria.builder()
                .field(fieldConfiguration)
                .value("testValue2")
                .build()))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("[(\n  \"t1\".\"test_column\" = 'testValue1'\n  and \"t1\".\"test_column\" = 'testValue2'\n)]"));
  }

  @Test
  void createFilterConditions_returnConditions_forInCriteria() {
    var filterCriteria = InFilterCriteria.builder()
        .field(fieldConfiguration)
        .values(List.of("testValue1", "testValue2"))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("[\"t1\".\"test_column\" in (\n  'testValue1', 'testValue2'\n)]"));
  }

  @Test
  void createFilterConditions_returnConditions_forGreaterThenFilterCriteria() {
    var filterCriteria = GreaterThenFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("[\"t1\".\"test_column\" > timestamp with time zone '2020-10-01 11:00:05+00:00']"));
  }

  @Test
  void createFilterConditions_returnConditions_forGreaterThenEqualsFilterCriteria() {
    var filterCriteria = GreaterThenEqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("[\"t1\".\"test_column\" >= timestamp with time zone '2020-10-01 11:00:05+00:00']"));
  }

  @Test
  void createFilterConditions_returnConditions_forLowerThenFilterCriteria() {
    var filterCriteria = LowerThenFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("[\"t1\".\"test_column\" < timestamp with time zone '2020-10-01 11:00:05+00:00']"));
  }

  @Test
  void createFilterConditions_returnConditions_forLowerThenEqualsFilterCriteria() {
    var filterCriteria = LowerThenEqualsFilterCriteria.builder()
        .field(fieldConfiguration)
        .value(OffsetDateTime.of(2020, 10, 1, 11, 0, 5, 0, ZoneOffset.UTC))
        .build();

    List<Condition> result = createFilterConditions(List.of(filterCriteria), fromTable);

    assertThat(result, notNullValue());
    assertThat(result.toString(),
        equalTo("[\"t1\".\"test_column\" <= timestamp with time zone '2020-10-01 11:00:05+00:00']"));
  }

  @Test
  void createFilterConditions_throwsException_forUnsupportedCriteria() {
    var filterCriteria = new FilterCriteria() {
      @Override
      public FieldConfiguration getField() {
        return null;
      }
    };

    List<FilterCriteria> filterCriterias = List.of(filterCriteria);

    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> createFilterConditions(filterCriterias, fromTable));

  }

}
