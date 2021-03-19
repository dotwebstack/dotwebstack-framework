package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.DISTINCT_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.SEPARATOR_ARGUMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateFieldFactoryTest {

  @Mock
  private PostgresFieldConfiguration postgresFieldConfiguration;

  private final AggregateFieldFactory aggregateFieldFactory = new AggregateFieldFactory();

  @Test
  void create_returnsGroupConcatDistinctField_ForArgumentStringJoinWithDistintIsTrue() {
    SelectedField mockedField = mockStringJoinField("stringJoin", Boolean.TRUE, "|");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "name", "");

    Field<?> expectedAggregateField = DSL.groupConcatDistinct(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatField_ForArgumentStringJoinWithDistintIsFalse() {
    SelectedField mockedField = mockStringJoinField("stringJoin", Boolean.FALSE, "|");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "name", "");

    Field<?> expectedAggregateField = DSL.groupConcat(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatFieldWithDefaultSeparator_ForArgumentStringJoin() {
    SelectedField mockedField = mockStringJoinField("stringJoin", null, null);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "name", "");

    Field<?> expectedAggregateField = DSL.groupConcat(DSL.field(DSL.name("beers", "name")))
        .separator(",");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinOnListTypeField() {
    SelectedField mockedField = mockStringJoinField("stringJoin", Boolean.FALSE, "|");
    when(postgresFieldConfiguration.isList()).thenReturn(Boolean.TRUE);

    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "taste", "x1");

    Field<?> expectedAggregateField = DSL.groupConcat(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinWithDistinctTrueOnListTypeField() {
    SelectedField mockedField = mockStringJoinField("stringJoin", Boolean.TRUE, "|");
    when(postgresFieldConfiguration.isList()).thenReturn(Boolean.TRUE);

    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "taste", "x1");

    Field<?> expectedAggregateField = DSL.groupConcatDistinct(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }


  @Test
  void create_returnsCountDistintField_ForArgumentCountWithDistintIsTrue() {
    SelectedField mockedField = mockCountField("count", Boolean.TRUE);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.countDistinct(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsCountField_ForArgumentCountWithDistintIsFalse() {
    SelectedField mockedField = mockCountField("count", Boolean.FALSE);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.count(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsCountField_ForArgumentCountWithoutDistintArgument() {
    SelectedField mockedField = mockCountField("count", null);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.count(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntSum() {
    SelectedField mockedField = mockSelectedField("intSum");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMin() {
    SelectedField mockedField = mockSelectedField("intMin");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMax() {
    SelectedField mockedField = mockSelectedField("intMax");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntAvg() {
    SelectedField mockedField = mockSelectedField("intAvg");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatSum() {
    SelectedField mockedField = mockSelectedField("floatSum");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMin() {
    SelectedField mockedField = mockSelectedField("floatMin");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMax() {
    SelectedField mockedField = mockSelectedField("floatMax");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatAvg() {
    SelectedField mockedField = mockSelectedField("floatAvg");
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(postgresFieldConfiguration, mockedField, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_throwsException_ForUnknownArgument() {
    assertThrows(IllegalArgumentException.class, () -> {
      SelectedField unknownField = mockSelectedField("unknownField");
      aggregateFieldFactory.create(postgresFieldConfiguration, unknownField, "beers", "soldPerYear", "");
    });
  }

  private SelectedField mockStringJoinField(String fieldName, Boolean isDistinct, String separator) {
    SelectedField stringJoinField = mockSelectedField(fieldName);
    Map<String, Object> arguments = new HashMap<>() {
      {
        put(DISTINCT_ARGUMENT, isDistinct);
        put(SEPARATOR_ARGUMENT, separator);
      }
    };
    when(stringJoinField.getArguments()).thenReturn(arguments);
    return stringJoinField;
  }

  private SelectedField mockCountField(String fieldName, Boolean isDistinct) {
    SelectedField countField = mockSelectedField(fieldName);
    Map<String, Object> arguments = Collections.singletonMap(DISTINCT_ARGUMENT, isDistinct);
    when(countField.getArguments()).thenReturn(arguments);
    return countField;
  }

  private SelectedField mockSelectedField(String fieldName) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(fieldName);
    return selectedField;
  }
}
