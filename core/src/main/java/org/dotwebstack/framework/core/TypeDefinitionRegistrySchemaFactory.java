package org.dotwebstack.framework.core;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.framework.core.config.TypeUtils.createType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;

import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldArgumentConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.QueryConfiguration;
import org.dotwebstack.framework.core.config.SubscriptionConfiguration;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.datafetchers.filter.FilterHelper;
import org.springframework.stereotype.Component;

@Component
public class TypeDefinitionRegistrySchemaFactory {
  private static final String QUERY_TYPE_NAME = "Query";

  private static final String SUBSCRIPTION_TYPE_NAME = "Subscription";

  private static final String GEOMETRY_TYPE = "Geometry";

  private static final String GEOMETRY_ARGUMENT_NAME = "type";

  private static final String GEOMETRY_ARGUMENT_TYPE = "GeometryType";

  private final DotWebStackConfiguration dotWebStackConfiguration;

  public TypeDefinitionRegistrySchemaFactory(DotWebStackConfiguration dotWebStackConfiguration) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
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
            typeDefinitionRegistry.add(createFilter(name, objectType));
          }
        });
  }

  private InputObjectTypeDefinition createFilter(String objectTypeName,
      AbstractTypeConfiguration<? extends FieldConfiguration> objectType) {
    var filterName = String.format("%sFilter", StringUtils.capitalize(objectTypeName));
    var inputObjectTypeDefinitionBuilder = newInputObjectDefinition().name(filterName);

    for (Map.Entry<String, FilterConfiguration> entry : objectType.getFilters()
        .entrySet()) {
      String type = objectType.getFields()
          .get(entry.getValue()
              .getField())
          .getType();

      var inputValueDefinition = InputValueDefinition.newInputValueDefinition()
          .name(entry.getKey())
          .type(newType(FilterHelper.getFilterNameForType(type)))
          .build();
      inputObjectTypeDefinitionBuilder.inputValueDefinition(inputValueDefinition);
    }

    return inputObjectTypeDefinitionBuilder.build();
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
    if (fieldConfiguration.getType()
        .equals(GEOMETRY_TYPE)) {
      return List.of(createGeometryInputValueDefinition());
    }

    return fieldConfiguration.getArguments()
        .stream()
        .map(this::createFieldInputValueDefinition)
        .collect(Collectors.toList());
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
        .fieldDefinitions(
            queryFieldDefinitions.isEmpty() ? List.of(createDummyQueryFieldDefinition()) : queryFieldDefinitions)
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
          var enumerationTypeDefinition = EnumTypeDefinition.newEnumTypeDefinition()
              .name(name)
              .enumValueDefinitions(enumeration.getValues()
                  .stream()
                  .map(value -> EnumValueDefinition.newEnumValueDefinition()
                      .name(value)
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

    // add query arguments for keys
    queryConfiguration.getKeys()
        .stream()
        .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectTypeConfiguration))
        .forEach(inputValueDefinitions::add);

    // add optional filter object
    if (queryConfiguration.isList() && !objectTypeConfiguration.getFilters()
        .isEmpty()) {
      var filterName = String.format("%sFilter", StringUtils.capitalize(queryConfiguration.getType()));

      InputValueDefinition inputValueDefinition = newInputValueDefinition().name("filter")
          .type(newType(filterName))
          .build();

      inputValueDefinitions.add(inputValueDefinition);
    }

    return newFieldDefinition().name(queryName)
        .type(createType(queryConfiguration))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private FieldDefinition createDummyQueryFieldDefinition() {
    return newFieldDefinition().name("dummy")
        .type(TypeUtils.newType("String"))
        .build();
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
}
