package org.dotwebstack.framework.core.datafetchers.aggregate;

import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.SelectedField;
import java.util.Map;
import org.dotwebstack.framework.core.RequestValidationException;
import org.dotwebstack.framework.core.config.EnumerationConfiguration;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateValidatorTest {

  private static final String FIELD_NAME = "aggregateField";

  private static final Map<String, EnumerationConfiguration> EMPTY_ENUM_CONFIG_MAP = Map.of();

  @Mock
  private SelectedField selectedField;

  @Mock
  private ObjectType<ObjectField> objectType;

  @Mock
  private ObjectField objectField;

  @ParameterizedTest()
  @ValueSource(strings = {INT_MIN_FIELD, INT_MAX_FIELD, INT_SUM_FIELD, INT_AVG_FIELD})
  void validate_shouldValidate_forIntFields(String aggregateFunction) {
    validate_shouldValidate_forNumericFields(GraphQLInt.getName(), aggregateFunction);
  }

  @ParameterizedTest()
  @ValueSource(strings = {FLOAT_MIN_FIELD, FLOAT_MAX_FIELD, FLOAT_SUM_FIELD, FLOAT_AVG_FIELD})
  void validate_shouldValidate_foFloatFields(String aggregateFunction) {
    validate_shouldValidate_forNumericFields(GraphQLFloat.getName(), aggregateFunction);
  }

  private void validate_shouldValidate_forNumericFields(String typeName, String aggregateFunction) {
    mockArguments(typeName, aggregateFunction);

    validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField);
  }

  @Test
  void validate_shouldThrowException_forNotNumericFields() {
    mockArguments(GraphQLString.getName(), INT_MIN_FIELD);

    var thrown = assertThrows(RequestValidationException.class,
        () -> validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField));

    assertThat(thrown.getMessage(),
        is(String.format("Numeric aggregation for non-numeric field %s is not supported.", FIELD_NAME)));
  }

  @Test
  void validate_shouldThrowException_forUnsupportedAggregationFunction() {
    mockArguments(GraphQLInt.getName(), "intRange");

    var thrown = assertThrows(RequestValidationException.class,
        () -> validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField));

    assertThat(thrown.getMessage(), is("Unsupported aggregation function: intRange."));
  }

  @Test
  void validate_shouldValidate_forCountField() {
    mockArguments(GraphQLString.getName(), COUNT_FIELD);

    validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField);
  }

  @Test
  void validate_shouldValidate_forStringJoinFieldWithStringType() {
    mockArguments(GraphQLString.getName(), STRING_JOIN_FIELD);

    validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField);
  }

  @Test
  void validate_shouldValidate_forStringJoinFieldWithEnumType() {
    String typeName = "EnumTest";
    mockArguments(typeName, STRING_JOIN_FIELD);

    validate(Map.of(typeName, mock(EnumerationConfiguration.class)), objectType, selectedField);
  }

  @Test
  void validate_shouldThrowException_forNotTextFields() {
    mockArguments(GraphQLInt.getName(), STRING_JOIN_FIELD);

    var thrown = assertThrows(RequestValidationException.class,
        () -> validate(EMPTY_ENUM_CONFIG_MAP, objectType, selectedField));

    assertThat(thrown.getMessage(),
        is(String.format("String aggregation for non-text field %s is not supported.", FIELD_NAME)));
  }

  private void mockArguments(String typeName, String aggregateFunction) {
    when(selectedField.getName()).thenReturn(aggregateFunction);
    when(selectedField.getArguments()).thenReturn(Map.of(AggregateConstants.FIELD_ARGUMENT, FIELD_NAME));
    when(objectType.getFields()).thenReturn(Map.of(FIELD_NAME, objectField));
    lenient().when(objectField.getType())
        .thenReturn(typeName);
    lenient().when(objectField.getName())
        .thenReturn(FIELD_NAME);
  }
}
