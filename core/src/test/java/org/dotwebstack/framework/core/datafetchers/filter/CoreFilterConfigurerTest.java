package org.dotwebstack.framework.core.datafetchers.filter;

import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoreFilterConfigurerTest {

  private CoreFilterConfigurer coreFilterConfigurer;

  private TypeDefinitionRegistry dataFetchingEnvironment;

  @BeforeEach
  void beforeAll() {
    coreFilterConfigurer = new CoreFilterConfigurer();
    dataFetchingEnvironment = new TypeDefinitionRegistry();
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forString() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, IN_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, STRING_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forStringList() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, CONTAINS_ALL_OF_FIELD, CONTAINS_ANY_OF_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, STRING_LIST_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forInt() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, IN_FIELD, LT_FIELD, LTE_FIELD, GT_FIELD, GTE_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, INT_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forIntList() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, CONTAINS_ALL_OF_FIELD, CONTAINS_ANY_OF_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, INT_LIST_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forFloat() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, IN_FIELD, LT_FIELD, LTE_FIELD, GT_FIELD, GTE_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, FLOAT_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forFloatList() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, CONTAINS_ALL_OF_FIELD, CONTAINS_ANY_OF_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, FLOAT_LIST_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forDate() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, LT_FIELD, LTE_FIELD, GT_FIELD, GTE_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, DATE_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forDateTime() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, LT_FIELD, LTE_FIELD, GT_FIELD, GTE_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, DATE_TIME_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forBoolean() {
    List<String> expectedFieldNames = List.of(EQ_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, BOOLEAN_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forStringPartial() {
    List<String> expectedFieldNames = List.of(MATCH_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, STRING_PARTIAL_FILTER_INPUT_OBJECT_TYPE);
  }

  @Test
  void configureTypeDefinitionRegistry_addFilter_forStringPartialList() {
    List<String> expectedFieldNames = List.of(MATCH_FIELD, NOT_FIELD);
    configureTypeDefinitionRegistry_addFilter_forType(expectedFieldNames, STRING_PARTIAL_LIST_FILTER_INPUT_OBJECT_TYPE);
  }

  @SuppressWarnings("rawtypes")
  private void configureTypeDefinitionRegistry_addFilter_forType(List<String> expectedFieldNames, String typeName) {
    coreFilterConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(typeName);
    assertThat(optional.isPresent(), is(true));
    assertThat(optional.get(), instanceOf(InputObjectTypeDefinition.class));
    InputObjectTypeDefinition inputObjectTypeDefinition = (InputObjectTypeDefinition) optional.get();

    List<String> actualFieldNames = inputObjectTypeDefinition.getInputValueDefinitions()
        .stream()
        .map(InputValueDefinition::getName)
        .collect(Collectors.toList());

    assertThat(actualFieldNames, containsInAnyOrder(expectedFieldNames.toArray()));
  }
}
