package org.dotwebstack.framework.core.datafetchers.filter;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static org.mockito.Mockito.mock;

import graphql.AssertException;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNamedInputType;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.FieldConfigurationImpl;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.junit.jupiter.api.BeforeAll;

abstract class FilterCriteriaParserBaseTest {

  private static final FilterConfigurer filterConfigurer = new FilterConfigurer();

  private static final TypeDefinitionRegistry dataFetchingEnvironment = new TypeDefinitionRegistry();

  static final String FIELD_DEFAULT_TEST = "fieldDefaultTest";

  static final String FIELD_TEST = "fieldTest";

  static final String FIELD_NULL_TEST = "nullFieldTest";

  @BeforeAll
  public static void init() {
    filterConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);
  }

  @SuppressWarnings("rawtypes")
  GraphQLInputObjectField createInputObjectField(String name, String typeName) {
    Optional<TypeDefinition> typeDefinition = dataFetchingEnvironment.getType(typeName);
    InputObjectTypeDefinition inputObjectTypeDefinition = (InputObjectTypeDefinition) typeDefinition
        .orElseThrow(() -> new AssertException(String.format("No type definition found for type name %s.", typeName)));

    List<GraphQLInputObjectField> fields = getFields(typeName, inputObjectTypeDefinition);

    GraphQLInputObjectType inputObjectType = GraphQLInputObjectType.newInputObject()
        .name(typeName)
        .definition(inputObjectTypeDefinition)
        .fields(fields)
        .build();

    return newInputObjectField().name(name)
        .type(inputObjectType)
        .build();
  }

  GraphQLInputObjectField createInputObjectField(String name, GraphQLNamedInputType type) {
    return newInputObjectField().name(name)
        .type(type)
        .build();
  }

  private List<GraphQLInputObjectField> getFields(String typeName,
      InputObjectTypeDefinition inputObjectTypeDefinition) {
    return inputObjectTypeDefinition.getInputValueDefinitions()
        .stream()
        .map(inputValueDefinition -> newInputObjectField().name(inputValueDefinition.getName())
            .type(GraphQLInputObjectType.newInputObject()
                .name(typeName)
                .fields(getOperators(inputObjectTypeDefinition))
                .build())
            .build())
        .collect(Collectors.toList());
  }

  private List<GraphQLInputObjectField> getOperators(InputObjectTypeDefinition inputObjectTypeDefinition) {
    return inputObjectTypeDefinition.getInputValueDefinitions()
        .stream()
        .map(inputValueDefinition -> GraphQLInputObjectField.newInputObjectField()
            .name(inputValueDefinition.getName())
            .type(mock(GraphQLInputType.class))
            .build())
        .collect(Collectors.toList());
  }

  TypeConfigurationImpl createTypeConfiguration(String type) {
    return createTypeConfiguration(type, null);
  }

  TypeConfigurationImpl createTypeConfiguration(String type, Object defaultValue) {
    TypeConfigurationImpl typeConfiguration = new TypeConfigurationImpl();
    typeConfiguration.setFilters(createFilters(defaultValue));
    typeConfiguration.setFields(createFieldConfigurations(type));
    return typeConfiguration;
  }

  private Map<String, FilterConfiguration> createFilters(Object defaultValue) {
    FilterConfiguration filterConfigurationDefault = createFilterConfiguration(FIELD_DEFAULT_TEST, defaultValue);
    FilterConfiguration filterConfigurationField = createFilterConfiguration(FIELD_TEST, null);
    FilterConfiguration filterConfigurationNullField = createFilterConfiguration(null, null);

    return Map.of(FIELD_DEFAULT_TEST, filterConfigurationDefault, FIELD_TEST, filterConfigurationField, FIELD_NULL_TEST,
        filterConfigurationNullField);
  }

  private FilterConfiguration createFilterConfiguration(String field, Object defaultValue) {
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.setDefaultValue(defaultValue);
    filterConfiguration.setField(field);

    return filterConfiguration;
  }

  private Map<String, FieldConfigurationImpl> createFieldConfigurations(String type) {
    FieldConfigurationImpl fieldConfigurationDefault = createFieldConfiguration(FIELD_DEFAULT_TEST, type);
    FieldConfigurationImpl fieldConfigurationField = createFieldConfiguration(FIELD_TEST, type);
    FieldConfigurationImpl fieldConfigurationNullField = createFieldConfiguration(FIELD_NULL_TEST, type);

    return Map.of(FIELD_DEFAULT_TEST, fieldConfigurationDefault, FIELD_TEST, fieldConfigurationField, FIELD_NULL_TEST,
        fieldConfigurationNullField);
  }

  private FieldConfigurationImpl createFieldConfiguration(String name, String type) {
    FieldConfigurationImpl fieldConfiguration = new FieldConfigurationImpl();
    fieldConfiguration.setName(name);
    fieldConfiguration.setType(type);

    return fieldConfiguration;
  }
}
