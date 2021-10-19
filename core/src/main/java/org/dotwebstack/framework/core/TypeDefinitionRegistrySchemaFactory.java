package org.dotwebstack.framework.core;

import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.framework.core.config.TypeUtils.createType;
import static org.dotwebstack.framework.core.config.TypeUtils.newListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;
import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_TYPE_SUFFIX;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;

import com.google.common.base.CaseFormat;
import graphql.Scalars;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValue;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.IntValue;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConfigurer;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterHelper;
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.FieldArgument;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Subscription;
import org.dotwebstack.framework.core.query.model.Query;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class TypeDefinitionRegistrySchemaFactory {
  private static final String QUERY_TYPE_NAME = "Query";

  private static final String SUBSCRIPTION_TYPE_NAME = "Subscription";

  private static final String GEOMETRY_TYPE = "Geometry";

  private static final String GEOMETRY_ARGUMENT_NAME = "type";

  private static final String GEOMETRY_ARGUMENT_TYPE = "GeometryType";

  private final Schema schema;

  private final Map<String, String> fieldFilterMap = new HashMap<>();

  public TypeDefinitionRegistrySchemaFactory(Schema schema, List<FilterConfigurer> filterConfigurers) {
    this.schema = schema;
    filterConfigurers.forEach(filterConfigurer -> filterConfigurer.configureFieldFilterMapping(fieldFilterMap));
  }

  public TypeDefinitionRegistry createTypeDefinitionRegistry() {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();

    addEnumerations(typeDefinitionRegistry);
    addObjectTypes(typeDefinitionRegistry);
    addFilterTypes(typeDefinitionRegistry);
    addSortTypes(typeDefinitionRegistry);
    addContextTypes(typeDefinitionRegistry);

    if (schema.usePaging()) {
      addConnectionTypes(typeDefinitionRegistry);
    }

    addQueryTypes(typeDefinitionRegistry);
    addSubscriptionTypes(typeDefinitionRegistry);

    return typeDefinitionRegistry;
  }

  private void addObjectTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> {

          var objectTypeDefinition = newObjectTypeDefinition().name(name)
              .fieldDefinitions(createFieldDefinitions(objectType));

          if (objectType.isNested()) {
            objectTypeDefinition.additionalData(GraphQlConstants.IS_NESTED, Boolean.TRUE.toString());
          }

          typeDefinitionRegistry.add(objectTypeDefinition.build());
        });
  }

  private void addFilterTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> {
          if (!objectType.getFilters()
              .isEmpty()) {
            typeDefinitionRegistry.add(createFilterObjectTypeDefinition(name, objectType));
          }
        });
  }

  private void addSortTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> {
          if (!objectType.getSortableBy()
              .isEmpty()) {
            typeDefinitionRegistry.add(createSortableByObjectTypeDefinition(name, objectType));
          }

        });
  }

  private void addContextTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getContexts()
        .entrySet()
        .stream()
        .map(entry -> createContextInputObjectTypeDefinition(entry.getKey(), entry.getValue()))
        .forEach(typeDefinitionRegistry::add);
  }

  private InputObjectTypeDefinition createContextInputObjectTypeDefinition(String contextName, Context context) {
    List<InputValueDefinition> inputValueDefinitions = context.getFields()
        .entrySet()
        .stream()
        .map(entry -> newInputValueDefinition().name(entry.getKey())
            .type(newNonNullableType(entry.getValue()
                .getType()))
            .defaultValue(StringValue.newStringValue(entry.getValue()
                .getDefaultValue())
                .build())
            .build())
        .collect(Collectors.toList());

    return newInputObjectDefinition().name(formatContextTypeName(contextName))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private void addConnectionTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .values()
        .stream()
        .map(this::createConnectionTypeDefinition)
        .forEach(typeDefinitionRegistry::add);
  }

  private ObjectTypeDefinition createConnectionTypeDefinition(ObjectType<?> objectType) {
    var connectionName = createConnectionName(objectType.getName());
    return newObjectTypeDefinition().name(connectionName)
        .fieldDefinition(newFieldDefinition().name(PagingConstants.NODES_FIELD_NAME)
            .type(newNonNullableListType(objectType.getName()))
            .build())
        .fieldDefinition(newFieldDefinition().name(PagingConstants.OFFSET_FIELD_NAME)
            .type(newNonNullableType(Scalars.GraphQLInt.getName()))
            .build())
        .additionalData(Map.of(GraphQlConstants.IS_CONNECTION_TYPE, Boolean.TRUE.toString()))
        .build();
  }

  private InputObjectTypeDefinition createFilterObjectTypeDefinition(String objectTypeName, ObjectType<?> objectType) {
    var filterName = createFilterName(objectTypeName);

    List<InputValueDefinition> inputValueDefinitions = objectType.getFilters()
        .entrySet()
        .stream()
        .map(entry -> newInputValueDefinition().name(entry.getKey())
            .type(newType(FilterHelper.getTypeNameForFilter(schema, fieldFilterMap, objectType, entry.getKey(),
                entry.getValue())))
            .build())
        .collect(Collectors.toList());

    return newInputObjectDefinition().name(filterName)
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private EnumTypeDefinition createSortableByObjectTypeDefinition(String objectTypeName, ObjectType<?> objectType) {
    var orderName = createOrderName(objectTypeName);

    List<EnumValueDefinition> enumValueDefinitions = getEnumValueDefinitions(objectType);

    return newEnumTypeDefinition().name(orderName)
        .enumValueDefinitions(enumValueDefinitions)
        .build();
  }

  private List<EnumValueDefinition> getEnumValueDefinitions(ObjectType<?> objectType) {
    return objectType.getSortableBy()
        .keySet()
        .stream()
        .map(key -> newEnumValueDefinition().name(key.toUpperCase())
            .build())
        .collect(Collectors.toList());
  }

  private List<FieldDefinition> createFieldDefinitions(ObjectType<?> objectType) {
    return objectType.getFields()
        .values()
        .stream()
        .flatMap(objectField -> createFieldDefinition(objectField).stream())
        .collect(Collectors.toList());
  }

  private Optional<FieldDefinition> createFieldDefinition(ObjectField objectField) {
    Type<?> type;
    if (StringUtils.isBlank(objectField.getType())) {
      if (AggregateHelper.isAggregate(objectField)) {
        type = TypeUtils.newType(AggregateConstants.AGGREGATE_TYPE);
      } else {
        return Optional.empty();
      }
    } else {
      type = createTypeForField(objectField);
    }

    return Optional.of(newFieldDefinition().name(objectField.getName())
        .type(type)
        .inputValueDefinitions(createInputValueDefinitions(objectField))
        .build());
  }

  private Type<?> createTypeForField(ObjectField objectField) {
    var type = objectField.getType();

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      return createListType(type, objectField.isNullable());
    }

    return createType(objectField);
  }

  private Type<?> createTypeForQuery(Query query) {
    var type = query.getType();

    if (query.isList()) {
      return createListType(type, false);
    }

    return createType(query);
  }

  private Type<?> createListType(String type, boolean nullable) {
    if (schema.usePaging()) {
      var connectionTypeName = createConnectionName(type);
      return newNonNullType(newType(connectionTypeName))
          .additionalData(GraphQlConstants.IS_CONNECTION_TYPE, Boolean.TRUE.toString())
          .build();
    } else {
      if (nullable) {
        return newListType(type);
      } else {
        return newNonNullableListType(type);
      }
    }
  }


  private List<InputValueDefinition> createInputValueDefinitions(ObjectField objectField) {
    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
    if (GEOMETRY_TYPE.equals(objectField.getType())) {
      inputValueDefinitions.add(createGeometryInputValueDefinition());
    }

    objectField.getArguments()
        .stream()
        .map(this::createFieldInputValueDefinition)
        .forEach(inputValueDefinitions::add);

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      var objectType = schema.getObjectType(objectField.getType())
          .orElseThrow();

      createInputValueDefinitionForFilteredObject(objectField.getType(), objectType)
          .ifPresent(inputValueDefinitions::add);

      createInputValueDefinitionForSortableByObject(objectField.getType(), objectType)
          .ifPresent(inputValueDefinitions::add);

      if (objectField.isList()) {
        createFirstArgument().ifPresent(inputValueDefinitions::add);
        createOffsetArgument().ifPresent(inputValueDefinitions::add);
      }

    }

    return inputValueDefinitions;
  }

  private void addQueryTypes(TypeDefinitionRegistry typeDefinitionRegistry) {

    var queryFieldDefinitions = schema.getQueries()
        .entrySet()
        .stream()
        .map(entry -> createQueryFieldDefinition(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    var queryTypeDefinition = newObjectTypeDefinition().name(QUERY_TYPE_NAME)
        .fieldDefinitions(
            queryFieldDefinitions.isEmpty() ? List.of(createDummyQueryFieldDefinition()) : queryFieldDefinitions)
        .build();

    typeDefinitionRegistry.add(queryTypeDefinition);
  }

  private void addSubscriptionTypes(TypeDefinitionRegistry typeDefinitionRegistry) {

    var subscriptionFieldDefinitions = schema.getSubscriptions()
        .entrySet()
        .stream()
        .map(entry -> createSubscriptionFieldDefinition(entry.getKey(), entry.getValue(), schema.getObjectTypes()
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

  private void addEnumerations(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getEnumerations()
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

  private FieldDefinition createSubscriptionFieldDefinition(String queryName, Subscription subscription,
      ObjectType<?> objectType) {
    var inputValueDefinitions = subscription.getKeys()
        .stream()
        .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectType))
        .collect(Collectors.toList());

    createInputValueDefinitionForFilteredObject(subscription.getType(), objectType)
        .ifPresent(inputValueDefinitions::add);

    addOptionalSortableByObject(subscription, objectType, inputValueDefinitions);

    if (StringUtils.isNotBlank(subscription.getContext())) {
      addOptionalContext(subscription.getContext(), inputValueDefinitions);
    }

    return newFieldDefinition().name(queryName)
        .type(createType(subscription))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private FieldDefinition createQueryFieldDefinition(String queryName, Query query) {

    var objectType = schema.getObjectType(query.getType())
        .orElseThrow();

    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();

    addQueryArgumentsForKeys(query, objectType, inputValueDefinitions);

    inputValueDefinitions.addAll(createPagingArguments(query));

    addOptionalFilterObject(query, objectType, inputValueDefinitions);

    addOptionalSortableByObject(query, objectType, inputValueDefinitions);

    if (StringUtils.isNotBlank(query.getContext())) {
      addOptionalContext(query.getContext(), inputValueDefinitions);
    }

    return newFieldDefinition().name(queryName)
        .type(createTypeForQuery(query))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private void addQueryArgumentsForKeys(Query query, ObjectType<?> objectType,
      List<InputValueDefinition> inputValueDefinitions) {
    query.getKeys()
        .stream()
        .map(keyConfiguration -> createQueryInputValueDefinition(keyConfiguration, objectType,
            Map.of(GraphQlConstants.IS_KEY_ARGUMENT, Boolean.TRUE.toString())))
        .forEach(inputValueDefinitions::add);
  }

  private void addOptionalFilterObject(Query query, ObjectType<?> objectType,
      List<InputValueDefinition> inputValueDefinitions) {
    if (query.isList()) {
      createInputValueDefinitionForFilteredObject(query.getType(), objectType).ifPresent(inputValueDefinitions::add);
    }
  }

  private Optional<InputValueDefinition> createInputValueDefinitionForFilteredObject(String typeName,
      ObjectType<?> objectType) {
    if (!objectType.getFilters()
        .isEmpty()) {
      var filterName = createFilterName(typeName);

      var inputValueDefinition = newInputValueDefinition().name(FilterConstants.FILTER_ARGUMENT_NAME)
          .type(newType(filterName))
          .build();

      return Optional.of(inputValueDefinition);
    }

    return Optional.empty();
  }

  private void addOptionalSortableByObject(Query query, ObjectType<?> objectType,
      List<InputValueDefinition> inputValueDefinitions) {
    if (query.isList()) {
      createInputValueDefinitionForSortableByObject(query.getType(), objectType).ifPresent(inputValueDefinitions::add);
    }
  }

  private void addOptionalSortableByObject(Subscription subscription, ObjectType<?> objectType,
      List<InputValueDefinition> inputValueDefinitions) {
    createInputValueDefinitionForSortableByObject(subscription.getType(), objectType)
        .ifPresent(inputValueDefinitions::add);
  }

  private Optional<InputValueDefinition> createInputValueDefinitionForSortableByObject(String typeName,
      ObjectType<?> objectType) {
    if (!objectType.getSortableBy()
        .isEmpty()) {
      var orderName = createOrderName(typeName);

      var firstSortableByArgument = objectType.getSortableBy()
          .keySet()
          .iterator()
          .next()
          .toString()
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

  private String formatContextTypeName(String contextName) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
        String.format("%s_%s", contextName, CONTEXT_TYPE_SUFFIX));
  }

  private void addOptionalContext(String contextName, List<InputValueDefinition> inputValueDefinitions) {
    inputValueDefinitions.add(newInputValueDefinition().name(CONTEXT_ARGUMENT_NAME)
        .type(newType(formatContextTypeName(contextName)))
        .defaultValue(ObjectValue.newObjectValue()
            .build())
        .additionalData("contextName", contextName)
        .build());
  }

  private List<InputValueDefinition> createPagingArguments(Query query) {
    if (query.isList()) {
      return Stream.concat(createFirstArgument().stream(), createOffsetArgument().stream())
          .collect(Collectors.toList());
    }

    return List.of();
  }

  private Optional<InputValueDefinition> createFirstArgument() {
    if (schema.usePaging()) {
      return Optional.of(newInputValueDefinition().name(PagingConstants.FIRST_ARGUMENT_NAME)
          .type(newType(Scalars.GraphQLInt.getName()))
          .defaultValue(IntValue.newIntValue(PagingConstants.FIRST_DEFAULT_VALUE)
              .build())
          .build());
    }

    return Optional.empty();
  }

  private Optional<InputValueDefinition> createOffsetArgument() {
    if (schema.usePaging()) {
      return Optional.of(newInputValueDefinition().name(PagingConstants.OFFSET_ARGUMENT_NAME)
          .type(newType(Scalars.GraphQLInt.getName()))
          .defaultValue(IntValue.newIntValue(PagingConstants.OFFSET_DEFAULT_VALUE)
              .build())
          .build());
    }
    return Optional.empty();
  }

  private InputValueDefinition createQueryInputValueDefinition(String keyField, ObjectType<?> objectType) {
    return createQueryInputValueDefinition(keyField, objectType, Map.of());
  }

  private InputValueDefinition createQueryInputValueDefinition(String keyField, ObjectType<?> objectType,
      Map<String, String> additionalData) {
    return newInputValueDefinition().name(keyField)
        .type(createType(keyField, objectType))
        .additionalData(additionalData)
        .build();
  }

  private InputValueDefinition createFieldInputValueDefinition(FieldArgument fieldArgument) {
    return newInputValueDefinition().name(fieldArgument.getName())
        .type(createType(fieldArgument))
        .build();
  }

  private InputValueDefinition createGeometryInputValueDefinition() {
    return newInputValueDefinition().name(GEOMETRY_ARGUMENT_NAME)
        .type(newType(GEOMETRY_ARGUMENT_TYPE))
        .build();
  }

  private String createFilterName(String objectTypeName) {
    return String.format("%sFilter", StringUtils.capitalize(objectTypeName));
  }

  private String createOrderName(String objectTypeName) {
    return String.format("%sOrder", StringUtils.capitalize(objectTypeName));
  }

  private FieldDefinition createDummyQueryFieldDefinition() {
    return newFieldDefinition().name("dummy")
        .type(newType("String"))
        .build();
  }


  private String createConnectionName(String objectTypeName) {
    return String.format("%sConnection", StringUtils.capitalize(objectTypeName));
  }
}
