package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateFieldHelperTest {

  @Test
  void create_returnsGroupConcatDistinctField_ForArgumentStringJoinWithDistintIsTrue() {
    var aggregateField = createStringJoinAggregateField(true, "|");
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "name", "");

    Field<?> expected = DSL.groupConcatDistinct(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsGroupConcatField_ForArgumentStringJoinWithDistintIsFalse() {
    var aggregateField = createStringJoinAggregateField(false, "|");
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "name", "");

    Field<?> expected = DSL.groupConcat(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinOnListTypeField() {
    var mockedField = mock(ObjectField.class);
    when(mockedField.isList()).thenReturn(Boolean.TRUE);
    var aggregateField = createStringJoinAggregateField(mockedField, false, "|");

    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "taste", "x1");

    Field<?> expected = DSL.groupConcat(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinWithDistinctTrueOnListTypeField() {
    var mockedField = mock(ObjectField.class);
    when(mockedField.isList()).thenReturn(Boolean.TRUE);
    var aggregateField = createStringJoinAggregateField(mockedField, true, "|");

    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "taste", "x1");

    Field<?> expected = DSL.groupConcatDistinct(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actual, is(expected));
  }


  @Test
  void create_returnsCountDistintField_ForArgumentCountWithDistintIsTrue() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.COUNT, ScalarType.INT, true);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.countDistinct(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsCountField_ForArgumentCountWithDistintIsFalse() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.COUNT, ScalarType.INT, false);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.count(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsSumField_ForArgumentIntSum() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.SUM, ScalarType.INT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsMinField_ForArgumentIntMin() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.INT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsMaxField_ForArgumentIntMax() {
    var maxAggregateField = createAggregateFieldConfiguration(AggregateFunctionType.MAX, ScalarType.INT);
    Field<?> actual = AggregateFieldHelper.create(maxAggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsAvgField_ForArgumentIntAvg() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.AVG, ScalarType.INT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatSum() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.SUM, ScalarType.FLOAT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsMinField_ForArgumentFloatMin() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.FLOAT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");
    Field<?> expected = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsMaxField_ForArgumentFloatMax() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.MAX, ScalarType.FLOAT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actual, is(expected));
  }

  @Test
  void create_returnsAvgField_ForArgumentFloatAvg() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.AVG, ScalarType.FLOAT);
    Field<?> actual = AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", "");

    Field<?> expected = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actual, is(expected));
  }

  @Test
  void create_throwsException_ForUnknownArgument() {
    var aggregateField = createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.STRING);
    assertThrows(IllegalArgumentException.class,
        () -> AggregateFieldHelper.create(aggregateField, "beers", "soldPerYear", ""));
  }

  private Class<? extends Number> getNumericType(ScalarType scalarType) {
    switch (scalarType) {
      case INT:
        return Integer.class;
      case FLOAT:
        return BigDecimal.class;
      default:
        throw illegalArgumentException("Type {} is not supported", scalarType);
    }
  }

  private AggregateField createStringJoinAggregateField(boolean isDistinct, String separator) {
    var mockedFieldConfiguration = mock(ObjectField.class);
    return createStringJoinAggregateField(mockedFieldConfiguration, isDistinct, separator);
  }

  private AggregateField createStringJoinAggregateField(ObjectField fieldConfiguration, boolean isDistinct,
      String separator) {
    return createAggregateFieldConfiguration(fieldConfiguration, AggregateFunctionType.JOIN, ScalarType.STRING,
        isDistinct, separator, "");
  }


  private AggregateField createAggregateFieldConfiguration(AggregateFunctionType aggregateFunctionType,
      ScalarType scalarType) {
    return createAggregateFieldConfiguration(aggregateFunctionType, scalarType, false);
  }

  private AggregateField createAggregateFieldConfiguration(AggregateFunctionType aggregateFunctionType,
      ScalarType scalarType, boolean distinct) {
    var mockedFieldConfiguration = mock(ObjectField.class);
    return createAggregateFieldConfiguration(mockedFieldConfiguration, aggregateFunctionType, scalarType, distinct,
        null, null);
  }

  private AggregateField createAggregateFieldConfiguration(ObjectField fieldConfiguration,
      AggregateFunctionType aggregateFunctionType, ScalarType scalarType, boolean distinct, String separator,
      String columnAlias) {
    return AggregateField.builder()
        .field(fieldConfiguration)
        .functionType(aggregateFunctionType)
        .type(scalarType)
        .distinct(distinct)
        .separator(separator)
        .alias(columnAlias)
        .build();

  }
}
