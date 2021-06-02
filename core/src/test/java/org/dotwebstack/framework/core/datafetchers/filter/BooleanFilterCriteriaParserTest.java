package org.dotwebstack.framework.core.datafetchers.filter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.junit.jupiter.api.Test;

class BooleanFilterCriteriaParserTest extends FilterCriteriaParserBaseTest {

  private final BooleanFilterCriteriaParser parser = new BooleanFilterCriteriaParser();

  @Test
  void supports_returnsTrue_forGraphQlBoolean() {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLBoolean);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(true));
  }

  @Test
  void supports_returnsFalse_forGraphQlString() {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLString);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(false));
  }

  @Test
  void parse_returnsFilterCriteriaList_forFieldFilter() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(Scalars.GraphQLBoolean.getName());
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLBoolean);
    Map<String, Object> data = Map.of(FIELD_TEST, true);

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(result.get(0)
        .getField()
        .getName(), is(FIELD_TEST));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(true));
  }

  @Test
  void parse_returnsFilterCriteriaList_forFieldFilterWithDefaultValue() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(Scalars.GraphQLBoolean.getName(), true);
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_DEFAULT_TEST, Scalars.GraphQLBoolean);
    Map<String, Object> data = Map.of();

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(result.get(0)
        .getField()
        .getName(), is(FIELD_DEFAULT_TEST));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(true));
  }

  @Test
  void parse_returnsFilterCriteriaList_forFieldFilterWithoutFilterFieldConfig() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(Scalars.GraphQLBoolean.getName());
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_NULL_TEST, Scalars.GraphQLBoolean);
    Map<String, Object> data = Map.of(FIELD_NULL_TEST, true);

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(EqualsFilterCriteria.class));
    assertThat(result.get(0)
        .getField()
        .getName(), is(FIELD_NULL_TEST));
    assertThat(((EqualsFilterCriteria) result.get(0)).getValue(), is(true));
  }

  @Test
  void parse_returnsEmptyFilterCriteriaList_forFieldFilterWithoutDataAndDefaultValue() {
    TypeConfigurationImpl typeConfiguration = createTypeConfiguration(Scalars.GraphQLBoolean.getName());
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLBoolean);
    Map<String, Object> data = Map.of();

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(0));
  }

}
