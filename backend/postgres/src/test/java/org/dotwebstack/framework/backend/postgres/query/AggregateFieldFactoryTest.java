package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateFieldFactoryTest {

  private final AggregateFieldFactory aggregateFieldFactory = new AggregateFieldFactory();

  @Test
  void create_returnsGroupConcatDistinctField_ForArgumentStringJoinWithDistintIsTrue() {
    var joinAggregateFieldConfiguration = createStringJoinAggregateFieldConfiguration(true, "|");
    Field<?> actualAggregateField = aggregateFieldFactory.create(joinAggregateFieldConfiguration, "beers", "name", "");

    Field<?> expectedAggregateField = DSL.groupConcatDistinct(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatField_ForArgumentStringJoinWithDistintIsFalse() {
    var joinAggregateFieldConfiguration = createStringJoinAggregateFieldConfiguration(false, "|");
    Field<?> actualAggregateField = aggregateFieldFactory.create(joinAggregateFieldConfiguration, "beers", "name", "");

    Field<?> expectedAggregateField = DSL.groupConcat(DSL.field(DSL.name("beers", "name")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinOnListTypeField() {
    var mockedFieldConfiguration = mock(FieldConfiguration.class);
    when(mockedFieldConfiguration.isList()).thenReturn(Boolean.TRUE);
    var joinAggregateFieldConfiguration =
        createStringJoinAggregateFieldConfiguration(mockedFieldConfiguration, false, "|");

    Field<?> actualAggregateField =
        aggregateFieldFactory.create(joinAggregateFieldConfiguration, "beers", "taste", "x1");

    Field<?> expectedAggregateField = DSL.groupConcat(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsGroupConcatFieldOnColumnAlias_ForArgumentStringJoinWithDistinctTrueOnListTypeField() {
    var mockedFieldConfiguration = mock(FieldConfiguration.class);
    when(mockedFieldConfiguration.isList()).thenReturn(Boolean.TRUE);
    var joinAggregateFieldConfiguration =
        createStringJoinAggregateFieldConfiguration(mockedFieldConfiguration, true, "|");

    Field<?> actualAggregateField =
        aggregateFieldFactory.create(joinAggregateFieldConfiguration, "beers", "taste", "x1");

    Field<?> expectedAggregateField = DSL.groupConcatDistinct(DSL.field(DSL.name("x1")))
        .separator("|");
    assertThat(actualAggregateField, is(expectedAggregateField));
  }


  @Test
  void create_returnsCountDistintField_ForArgumentCountWithDistintIsTrue() {
    var countAggregateFieldConfiguration =
        createAggregateFieldConfiguration(AggregateFunctionType.COUNT, ScalarType.INT, true);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(countAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.countDistinct(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsCountField_ForArgumentCountWithDistintIsFalse() {
    var countAggregateFieldConfiguration =
        createAggregateFieldConfiguration(AggregateFunctionType.COUNT, ScalarType.INT, false);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(countAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.count(DSL.field(DSL.name("beers", "soldPerYear")));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntSum() {
    var sumAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.SUM, ScalarType.INT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(sumAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsMinField_ForArgumentIntMin() {
    var minAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.INT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(minAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsMaxField_ForArgumentIntMax() {
    var maxAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.MAX, ScalarType.INT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(maxAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsAvgField_ForArgumentIntAvg() {
    var avgAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.AVG, ScalarType.INT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(avgAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatSum() {
    var sumAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.SUM, ScalarType.FLOAT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(sumAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsMinField_ForArgumentFloatMin() {
    var minAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.FLOAT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(minAggregateFieldConfiguration, "beers", "soldPerYear", "");
    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsMaxField_ForArgumentFloatMax() {
    var maxAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.MAX, ScalarType.FLOAT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(maxAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsAvgField_ForArgumentFloatAvg() {
    var avgAggregateFieldConfiguration = createAggregateFieldConfiguration(AggregateFunctionType.AVG, ScalarType.FLOAT);
    Field<?> actualAggregateField =
        aggregateFieldFactory.create(avgAggregateFieldConfiguration, "beers", "soldPerYear", "");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(getNumericType(ScalarType.FLOAT));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_throwsException_ForUnknownArgument() {
    var unknownTypeAggregateFieldConfiguration =
        createAggregateFieldConfiguration(AggregateFunctionType.MIN, ScalarType.STRING);
    assertThrows(IllegalArgumentException.class,
        () -> aggregateFieldFactory.create(unknownTypeAggregateFieldConfiguration, "beers", "soldPerYear", ""));
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

  private AggregateFieldConfiguration createStringJoinAggregateFieldConfiguration(boolean isDistinct,
      String separator) {
    var mockedFieldConfiguration = mock(FieldConfiguration.class);
    return createStringJoinAggregateFieldConfiguration(mockedFieldConfiguration, isDistinct, separator);
  }

  private AggregateFieldConfiguration createStringJoinAggregateFieldConfiguration(FieldConfiguration fieldConfiguration,
      boolean isDistinct, String separator) {
    return createAggregateFieldConfiguration(fieldConfiguration, AggregateFunctionType.JOIN, ScalarType.STRING,
        isDistinct, separator, "");
  }


  private AggregateFieldConfiguration createAggregateFieldConfiguration(AggregateFunctionType aggregateFunctionType,
      ScalarType scalarType) {
    return createAggregateFieldConfiguration(aggregateFunctionType, scalarType, false);
  }

  private AggregateFieldConfiguration createAggregateFieldConfiguration(AggregateFunctionType aggregateFunctionType,
      ScalarType scalarType, boolean distinct) {
    var mockedFieldConfiguration = mock(FieldConfiguration.class);
    return createAggregateFieldConfiguration(mockedFieldConfiguration, aggregateFunctionType, scalarType, distinct,
        null, null);
  }

  private AggregateFieldConfiguration createAggregateFieldConfiguration(FieldConfiguration fieldConfiguration,
      AggregateFunctionType aggregateFunctionType, ScalarType scalarType, boolean distinct, String separator,
      String colunnAlias) {
    return AggregateFieldConfiguration.builder()
        .field(fieldConfiguration)
        .aggregateFunctionType(aggregateFunctionType)
        .type(scalarType)
        .distinct(distinct)
        .separator(separator)
        .alias(colunnAlias)
        .build();

  }
}
