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
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.schema.SelectedField;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateValidatorTest {

  @Mock
  SelectedField selectedField;

  @Mock
  AbstractFieldConfiguration aggregateFieldConfiguration;

  @ParameterizedTest()
  @ValueSource(strings = {INT_MIN_FIELD, INT_MAX_FIELD, INT_SUM_FIELD, INT_AVG_FIELD, FLOAT_MIN_FIELD, FLOAT_MAX_FIELD,
      FLOAT_SUM_FIELD, FLOAT_AVG_FIELD})
  void validate_shouldValidate_forNumericFields(String aggregateFunction) {
    when(selectedField.getName()).thenReturn(aggregateFunction);
    when(aggregateFieldConfiguration.isNumeric()).thenReturn(true);

    validate(aggregateFieldConfiguration, selectedField);

    verify(selectedField).getName();
    verify(aggregateFieldConfiguration).isNumeric();
  }

  @Test
  void validate_shouldThrowException_forNotNumericFields() {
    when(selectedField.getName()).thenReturn(INT_MIN_FIELD);
    when(aggregateFieldConfiguration.isNumeric()).thenReturn(false);
    when(aggregateFieldConfiguration.getName()).thenReturn("name");

    var thrown =
        assertThrows(IllegalArgumentException.class, () -> validate(aggregateFieldConfiguration, selectedField));

    assertThat(thrown.getMessage(), is("Numeric aggregation for non-numeric field name is not supported."));
  }

  @Test
  void validate_shouldThrowException_forUnsupportedAggregationFunction() {
    when(selectedField.getName()).thenReturn("intRange");

    var thrown =
        assertThrows(IllegalArgumentException.class, () -> validate(aggregateFieldConfiguration, selectedField));

    assertThat(thrown.getMessage(), is("Unsupported aggregation function: intRange."));
  }

  @Test
  void validate_shouldValidate_forCountField() {
    when(selectedField.getName()).thenReturn(COUNT_FIELD);

    validate(aggregateFieldConfiguration, selectedField);

    verify(selectedField, times(2)).getName();
  }

  @Test
  void validate_shouldValidate_forStringJoinField() {
    when(selectedField.getName()).thenReturn(STRING_JOIN_FIELD);
    when(aggregateFieldConfiguration.isText()).thenReturn(true);

    validate(aggregateFieldConfiguration, selectedField);

    verify(selectedField, times(2)).getName();
    verify(aggregateFieldConfiguration).isText();
  }

  @Test
  void validate__shouldThrowException_forNotTextFields() {
    when(selectedField.getName()).thenReturn(STRING_JOIN_FIELD);
    when(aggregateFieldConfiguration.isText()).thenReturn(false);
    when(aggregateFieldConfiguration.getName()).thenReturn("soldPerYear");

    var thrown =
        assertThrows(IllegalArgumentException.class, () -> validate(aggregateFieldConfiguration, selectedField));

    assertThat(thrown.getMessage(), is("String aggregation for non-text field soldPerYear is not supported."));
  }
}
