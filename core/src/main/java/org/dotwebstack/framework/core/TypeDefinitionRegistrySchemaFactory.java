package org.dotwebstack.framework.core;

import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.framework.core.config.TypeUtils.createType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;

import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValue;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldArgumentConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.QueryConfiguration;
import org.dotwebstack.framework.core.config.SubscriptionConfiguration;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConfigurer;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterHelper;
import org.springframework.stereotype.Component;

@Component
public class TypeDefinitionRegistrySchemaFactory {
  private static final String QUERY_TYPE_NAME = "Query";

  private static final String SUBSCRIPTION_TYPE_NAME = "Subscription";

  private static final String GEOMETRY_TYPE = "Geometry";

  private static final String GEOMETRY_ARGUMENT_NAME = "type";

  private static final String GEOMETRY_ARGUMENT_TYPE = "GeometryType";

  private static final String SORT_ARGUMENT_NAME = "sort";

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final Map<String, String> fieldFilterMap = new HashMap<>();

  public TypeDefinitionRegistrySchemaFactory(DotWebStackConfiguration dotWebStackConfiguration,
      List<FilterConfigurer> filterConfigurers) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    filterConfigurers.forEach(filterConfigurer -> filterConfigurer.configureFieldFilterMapping(fieldFilterMap));
  }

  public TypeDefinitionRegistry createTypeDefinitionRegistry() {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();

    addEnumerations(dotWebStackConfiguration, typeDefinitionRegistry);
    addObjectTypes(dotWebStackConfiguration, typeDefinitionRegistry);
    addQueryTypes(dotWebStackConfiguration, typeDefinitionRegistry);
    addSubscriptionTypes(dotWebStackConfiguration, typeDefinitionRegistry);

    return typeDefinitionRegistry;
  }

  private void addObjectTypes(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {
    dotWebStackConfiguration.getObjectTypes()
        .forEach((name, objectType) -> {
          var objectTypeDefinition = newObjectTypeDefinition().name(name)
              .fieldDefinitions(createFieldDefinitions(objectType))
              .build();

          objectType.init(dotWebStackConfiguration, objectTypeDefinition);
          typeDefinitionRegistry.add(objectTypeDefinition);

          if (!objectType.getFilters()
              .isEmpty()) {
            typeDefinitionRegistry.add(createFilterObjectTypeDefinition(name, objectType));
          }

          if (!objectType.getSortableBy()
              .isEmpty()) {
            typeDefinitionRegistry.add(createSortableByObjectTypeDefinition(name, objectType));
          }

        });
  }

  private InputObjectTypeDefinition createFilterObjectTypeDefinition(String objectTypeName,
      AbstractTypeConfiguration<? extends FieldConfiguration> objectType) {
    var filterName = createFilterName(objectTypeName);

    List<InputValueDefinition> inputValueDefinitions = objectType.getFilters()
        .entrySet()
        .stream()
        .map(entry -> newInputValueDefinition().name(entry.getKey())
            .type(newType(
                FilterHelper.getTypeNameForFilter(fieldFilterMap, objectType, entry.getKey(), entry.getValue())))
            .build())
        .collect(Collectors.toList());

    return newInputObjectDefinition().name(filterName)
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private EnumTypeDefinition createSortableByObjectTypeDefinition(String objectTypeName,
      AbstractTypeConfiguration<? extends FieldConfiguration> objectType) {
    var orderName = createOrderName(objectTypeName);

    List<EnumValueDefinition> enumValueDefinitions = getEnumValueDefinitions(objectType);

    return newEnumTypeDefinition().name(orderName)
        .enumValueDefinitions(enumValueDefinitions)
        .build();
  }

  private List<EnumValueDefinition> getEnumValueDefinitions(
      AbstractTypeConfiguration<? extends FieldConfiguration> objectType) {
    return objectType.getSortableBy()
        .keySet()
        .stream()
        .map(key -> newEnumValueDefinition().name(key.toUpperCase())
            .build())
        .collect(Collectors.toList());
  }

  private List<FieldDefinition> createFieldDefinitions(
      AbstractTypeConfiguration<? extends FieldConfiguration> typeConfiguration) {
    return typeConfiguration.getFields()
        .entrySet()
        .stream()
        .map(entry -> newFieldDefinition().name(entry.getKey())
            .type(createType(entry.getValue()))
            .inputValueDefinitions(createInputValueDefinitions(entry.getValue()))
            .build())
        .collect(Collectors.toList());
  }

  private List<InputValueDefinition> createInputValueDefinitions(FieldConfiguration fieldConfiguration) {
    List<InputValueDefinition> result = new ArrayList<>();
    if (fieldConfiguration.getType()
        .equals(GEOMETRY_TYPE)) {
      result.add(createGeometryInputValueDefinition());
    }

    fieldConfiguration.getArguments()
        .stream()
        .map(this::createFieldInputValueDefinition)
        .forEach(result::add);

    if (fieldConfiguration.isList() && dotWebStackConfiguration.getObjectTypes()
        .containsKey(fieldConfiguration.getType())) {
      AbstractTypeConfiguration<?> objectTypeConfiguration =
          dotWebStackConfiguration.getTypeConfiguration(fieldConfiguration.getType());

      createInputValueDefinitionForFilteredObject(fieldConfiguration.getType(), objectTypeConfiguration)
          .ifPresent(result::add);
    }

    return result;
  }

  private void addQueryTypes(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {

    var queryFieldDefinitions = dotWebStackConfiguration.getQueries()
        .entrySet()
        .stream()
        .map(entry -> createQueryFieldDefinition(entry.getKey(), entry.getValue(),
            dotWebStackConfiguration.getObjectTypes()
                .get(entry.getValue()
                    .getType())))
        .collect(Collectors.toList());

    var queryTypeDefinition = newObjectTypeDefinition().name(QUERY_TYPE_NAME)
        .fieldDefinitions(queryFieldDefinitions.isEmpty() ? List.of() : queryFieldDefinitions)
        .build();

    typeDefinitionRegistry.add(queryTypeDefinition);
  }

  private void addSubscriptionTypes(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {

    var subscriptionFieldDefinitions = dotWebStackConfiguration.getSubscriptions()
        .entrySet()
        .stream()
        .map(entry -> createSubscriptionFieldDefinition(entry.getKey(), entry.getValue(),
            dotWebStackConfiguration.getObjectTypes()
                .get(entry.getValue()
                    .getType())))
        .collect(Collectors.toList());

    if (!subscriptionFieldDefinitions.isEmpty()) {
      var subscriptionTypeDefinition = newObjectTypeDefinition().name(SUBSCRIPTION_TYPE_NAME)
          .fieldDefinitions(subscriptionFieldDefinitions)
          .build();

      typeDefinitionRegistry.add(subscriptionTypeDefinition);
    }
  }

  private void addEnumerations(DotWebStackConfiguration dotWebStackConfiguration,
      TypeDefinitionRegistry typeDefinitionRegistry) {
    dotWebStackConfiguration.getEnumerations()
        .forEach((name, enumeration) -> {
          var enumerationTypeDefinition = newEnumTypeDefinition().name(name)
              .enumValueDefinitions(enumeration.getValues()
                  .stream()
                  .map(value -> newEnumValueDefinition().name(value)
                      .build())
                  .collect(Collectors.toList()))
              .build();

          typeDefinitionRegistry.add(enumerationTypeDefinition);
        });
  }

  private FieldDefinition createSubscriptionFieldDefinition(String queryName,
      SubscriptionConfiguration subscriptionConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    return newFieldDefinition().name(queryName)
        .type(createType(subscriptionConfiguration))
        .inputValueDefinitions(subscriptionConfiguration.getKeys()
            .stream()
            .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectTypeConfiguration))
            .collect(Collectors.toList()))
        .build();
  }

  private FieldDefinition createQueryFieldDefinition(String queryName, QueryConfiguration queryConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {

    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();

    addQueryArgumentsForKeys(queryConfiguration, objectTypeConfiguration, inputValueDefinitions);

    addOptionalFilterObject(queryConfiguration, objectTypeConfiguration, inputValueDefinitions);

    addOptionalSortableByObject(queryConfiguration, objectTypeConfiguration, inputValueDefinitions);

    return newFieldDefinition().name(queryName)
        .type(createType(queryConfiguration))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private void addQueryArgumentsForKeys(QueryConfiguration queryConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration,
      List<InputValueDefinition> inputValueDefinitions) {
    queryConfiguration.getKeys()
        .stream()
        .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectTypeConfiguration))
        .forEach(inputValueDefinitions::add);
  }

  private void addOptionalFilterObject(QueryConfiguration queryConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration,
      List<InputValueDefinition> inputValueDefinitions) {
    if (queryConfiguration.isList()) {
      createInputValueDefinitionForFilteredObject(queryConfiguration.getType(), objectTypeConfiguration)
          .ifPresent(inputValueDefinitions::add);
    }
  }

  private Optional<InputValueDefinition> createInputValueDefinitionForFilteredObject(String typeName,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    if (!objectTypeConfiguration.getFilters()
        .isEmpty()) {
      var filterName = createFilterName(typeName);

      var inputValueDefinition = newInputValueDefinition().name(FilterConstants.FILTER_ARGUMENT_NAME)
          .type(newType(filterName))
          .build();

      return Optional.of(inputValueDefinition);
    }

    return Optional.empty();
  }

  private void addOptionalSortableByObject(QueryConfiguration queryConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration,
      List<InputValueDefinition> inputValueDefinitions) {
    if (queryConfiguration.isList()) {
      createInputValueDefinitionForSortableByObject(queryConfiguration.getType(), objectTypeConfiguration)
          .ifPresent(inputValueDefinitions::add);
    }
  }

  private Optional<InputValueDefinition> createInputValueDefinitionForSortableByObject(String typeName,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    if (!objectTypeConfiguration.getSortableBy()
        .isEmpty()) {
      var orderName = createOrderName(typeName);

      var firstSortableByArgument = objectTypeConfiguration.getSortableBy()
          .keySet()
          .iterator()
          .next()
          .toUpperCase();

      var inputValueDefinition = newInputValueDefinition().name(SORT_ARGUMENT_NAME)
          .type(newType(orderName))
          .defaultValue(EnumValue.newEnumValue(firstSortableByArgument)
              .build())
          .build();

      return Optional.of(inputValueDefinition);
    }

    return Optional.empty();
  }

  private InputValueDefinition createQueryInputValueDefinition(KeyConfiguration keyConfiguration,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> objectTypeConfiguration) {
    return newInputValueDefinition().name(keyConfiguration.getField())
        .type(createType(keyConfiguration.getField(), objectTypeConfiguration))
        .build();
  }

  private InputValueDefinition createFieldInputValueDefinition(FieldArgumentConfiguration fieldArgumentConfiguration) {
    return newInputValueDefinition().name(fieldArgumentConfiguration.getName())
        .type(createType(fieldArgumentConfiguration))
        .build();
  }

  private InputValueDefinition createGeometryInputValueDefinition() {
    return newInputValueDefinition().name(GEOMETRY_ARGUMENT_NAME)
        .type(TypeUtils.newType(GEOMETRY_ARGUMENT_TYPE))
        .build();
  }

  private String createFilterName(String objectTypeName) {
    return String.format("%sFilter", StringUtils.capitalize(objectTypeName));
  }

  private String createOrderName(String objectTypeName) {
    return String.format("%sOrder", StringUtils.capitalize(objectTypeName));
  }
}
