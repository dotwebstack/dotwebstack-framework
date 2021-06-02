package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.EQ_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.IN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.NOT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.query.model.filter.AndFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.GreaterThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.InFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenEqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.LowerThenFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoreFilterCriteriaParserTest extends FilterCriteriaParserBaseTest {

  public static final String INT_TYPE = "Int";

  private final CoreFilterCriteriaParser parser = new CoreFilterCriteriaParser();

  @ParameterizedTest
  @CsvSource({STRING_FILTER_INPUT_OBJECT_TYPE, DATE_FILTER_INPUT_OBJECT_TYPE, INT_FILTER_INPUT_OBJECT_TYPE,
      FLOAT_FILTER_INPUT_OBJECT_TYPE, DATE_TIME_FILTER_INPUT_OBJECT_TYPE})
  void supports_returnsTrue_forSupportedScalar(String typeName) {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, typeName);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(true));
  }

  @Test
  void supports_returnsFalse_forUnsupportedScalar() {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLBoolean);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(false));
  }

  @ParameterizedTest
  @CsvSource({"String, testString, eq", "Date, 2021-01-01, eq", "Int, 1234, eq", "Float, 12345, eq",
      "DateTime, 2021-01-01T00:00:00+02:00, eq"})
  void parse_returnsFilterCriteriaList_forDifferentFilters(String type, String value) {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(type);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, type + "Filter");
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of("eq", value));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertFieldName(result.get(0));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(value));
  }

  @Test
  void parse_returnsFilterCriteriaList_forSimpleOperators() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data =
        Map.of(FIELD_TEST, Map.of(EQ_FIELD, 1, LT_FIELD, 2, LTE_FIELD, 3, GT_FIELD, 4, GTE_FIELD, 5));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);
    result.sort(Comparator.comparing(o -> o.getClass()
        .getName()));

    assertThat(result.size(), is(5));

    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertFieldName(result.get(0));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(1));

    assertThat(result.get(1), instanceOf(GreaterThenEqualsFilterCriteria.class));
    assertFieldName(result.get(1));
    assertThat(((GreaterThenEqualsFilterCriteria) result.get(1)).getValue(), is(5));

    assertThat(result.get(2), instanceOf(GreaterThenFilterCriteria.class));
    assertFieldName(result.get(2));
    assertThat(((GreaterThenFilterCriteria) result.get(2)).getValue(), is(4));

    assertThat(result.get(3), instanceOf(LowerThenEqualsFilterCriteria.class));
    assertFieldName(result.get(3));
    assertThat(((LowerThenEqualsFilterCriteria) result.get(3)).getValue(), is(3));

    assertThat(result.get(4), instanceOf(LowerThenFilterCriteria.class));
    assertFieldName(result.get(4));
    assertThat(((LowerThenFilterCriteria) result.get(4)).getValue(), is(2));
  }

  @Test
  void parse_returnsFilterCriteriaList_forInOperator() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(IN_FIELD, List.of(1, 2)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));

    assertThat(result.get(0), instanceOf(InFilterCriteria.class));
    assertFieldName(result.get(0));
    assertThat(((InFilterCriteria) result.get(0)).getValues()
        .size(), is(2));
    assertThat(((InFilterCriteria) result.get(0)).getValues(), containsInAnyOrder(1, 2));
  }

  @Test
  void parse_throwsIllegalArgumentException_forInOperatorWithNonList() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(IN_FIELD, 1));

    assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));
  }

  @Test
  void parse_returnsFilterCriteriaList_forNotOperator() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(NOT_FIELD, Map.of(EQ_FIELD, 1)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertFieldName(result.get(0));

    assertThat(result.get(0), instanceOf(NotFilterCriteria.class));
    NotFilterCriteria notFilterCriteria = (NotFilterCriteria) result.get(0);

    assertThat(notFilterCriteria.getFilterCriteria(), instanceOf(EqualsFilterCriteria.class));
    assertThat(notFilterCriteria.getFilterCriteria()
        .getField(), instanceOf(FieldConfigurationImpl.class));
    assertThat(((EqualsFilterCriteria) notFilterCriteria.getFilterCriteria()).getValue(), is(1));
  }

  @Test
  void parse_returnsFilterCriteriaList_forNotOperatorWithMultipleInnerOperators() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(NOT_FIELD, Map.of(EQ_FIELD, 1, LT_FIELD, 2)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));

    assertThat(result.get(0), instanceOf(NotFilterCriteria.class));
    NotFilterCriteria notFilterCriteria = (NotFilterCriteria) result.get(0);

    assertThat(notFilterCriteria.getFilterCriteria(), instanceOf(AndFilterCriteria.class));
    AndFilterCriteria andFilterCriteria = (AndFilterCriteria) notFilterCriteria.getFilterCriteria();
    assertThat(andFilterCriteria.getFilterCriterias()
        .size(), is(2));

    assertThat(andFilterCriteria.getFilterCriterias()
        .get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(andFilterCriteria.getFilterCriterias()
        .get(0)
        .getField(), instanceOf(FieldConfigurationImpl.class));
    assertThat(((EqualsFilterCriteria) andFilterCriteria.getFilterCriterias()
        .get(0)).getValue(), is(1));

    assertThat(andFilterCriteria.getFilterCriterias()
        .get(1), instanceOf(LowerThenFilterCriteria.class));
    assertThat(andFilterCriteria.getFilterCriterias()
        .get(1)
        .getField(), instanceOf(FieldConfigurationImpl.class));
    assertThat(((LowerThenFilterCriteria) andFilterCriteria.getFilterCriterias()
        .get(1)).getValue(), is(2));
  }

  @Test
  void parse_returnsFilterCriteriaList_forFieldFilterWithDefaultValue() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE, Map.of(EQ_FIELD, 99));
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_DEFAULT_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of();

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(result.get(0)
        .getField()
        .getName(), is(FIELD_DEFAULT_TEST));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(99));
  }

  @Test
  void parse_returnsFilterCriteriaList_forFieldFilterWithoutFilterFieldConfig() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_NULL_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of(FIELD_NULL_TEST, Map.of(EQ_FIELD, 1));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(result.get(0)
        .getField()
        .getName(), is(FIELD_NULL_TEST));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(1));
  }

  @Test
  void parse_returnsEmptyFilterCriteriaList_forFieldFilterWithoutDataAndDefaultValue() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(INT_TYPE);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, INT_FILTER_INPUT_OBJECT_TYPE);
    Map<String, Object> data = Map.of();

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(0));
  }

  private void assertFieldName(FilterCriteria filterCriteria) {
    assertThat(filterCriteria.getField()
        .getName(), is(FIELD_TEST));
  }
}
