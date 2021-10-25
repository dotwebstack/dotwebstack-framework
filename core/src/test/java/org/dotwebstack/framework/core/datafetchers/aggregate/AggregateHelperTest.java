package org.dotwebstack.framework.core.datafetchers.aggregate;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.SelectedField;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateHelperTest {

  @ParameterizedTest()
  @ValueSource(strings = {INT_MIN_FIELD, INT_MAX_FIELD, INT_SUM_FIELD, INT_AVG_FIELD, COUNT_FIELD})
  void getAggregateScalarType_returnsIntType_forIntFields(String aggregateFunction) {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(aggregateFunction);

    var scalarType = getAggregateScalarType(selectedField);

    assertThat(scalarType, is(ScalarType.INT));
  }

  @ParameterizedTest()
  @ValueSource(strings = {FLOAT_MIN_FIELD, FLOAT_MAX_FIELD, FLOAT_SUM_FIELD, FLOAT_AVG_FIELD})
  void getAggregateScalarType_returnsFloatType_forFloatFields(String aggregateFunction) {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(aggregateFunction);

    var scalarType = getAggregateScalarType(selectedField);

    assertThat(scalarType, is(ScalarType.FLOAT));
  }

  @Test
  void getAggregateScalarType_returnsStringType_forStringJoin() {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn("stringJoin");

    var scalarType = getAggregateScalarType(selectedField);

    assertThat(scalarType, is(ScalarType.STRING));
  }

  @Test
  void getAggregateScalarType_throwsException_forUnsupportedFunction() {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn("intRange");

    var thrown = assertThrows(IllegalArgumentException.class, () -> getAggregateScalarType(selectedField));

    assertThat(thrown.getMessage(), is("Aggregate function intRange is not supported"));
  }

  @ParameterizedTest
  @MethodSource("aggregateTypes")
  void getAggregateFunctionType_returnsFunctionType_forAggregateFunction(String aggregateFunction,
      AggregateFunctionType aggregateFunctionType) {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(aggregateFunction);

    var result = getAggregateFunctionType(selectedField);

    assertThat(result, is(aggregateFunctionType));
  }

  @Test
  void getAggregateFunctionType_throwsException_forUnsupportedFunction() {
    var selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn("intRange");

    var thrown = assertThrows(IllegalArgumentException.class, () -> getAggregateFunctionType(selectedField));

    assertThat(thrown.getMessage(), is("Aggregate function intRange is not supported"));
  }

  private static Stream<Arguments> aggregateTypes() {
    return Stream.of(arguments(COUNT_FIELD, AggregateFunctionType.COUNT),
        arguments(INT_AVG_FIELD, AggregateFunctionType.AVG), arguments(INT_MAX_FIELD, AggregateFunctionType.MAX),
        arguments(INT_MIN_FIELD, AggregateFunctionType.MIN), arguments(INT_SUM_FIELD, AggregateFunctionType.SUM),
        arguments(FLOAT_AVG_FIELD, AggregateFunctionType.AVG), arguments(FLOAT_MIN_FIELD, AggregateFunctionType.MIN),
        arguments(FLOAT_MAX_FIELD, AggregateFunctionType.MAX), arguments(FLOAT_SUM_FIELD, AggregateFunctionType.SUM),
        arguments(STRING_JOIN_FIELD, AggregateFunctionType.JOIN));
  }
}
