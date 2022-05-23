package org.dotwebstack.framework.core;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static java.lang.Boolean.TRUE;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.framework.core.config.TypeUtils.createType;
import static org.dotwebstack.framework.core.config.TypeUtils.newListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;
import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_TYPE_SUFFIX;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FILTER_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.OR_FIELD;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_DEFAULT_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.NODES_FIELD_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_DEFAULT_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_FIELD_NAME;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_BATCH_KEY_QUERY;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_CONNECTION_TYPE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_NESTED;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_PAGING_NODE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_FIELD;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_PATH;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getFieldKey;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getObjectType;
import static org.dotwebstack.framework.core.helpers.TypeHelper.QUERY_TYPE_NAME;
import static org.dotwebstack.framework.core.helpers.TypeHelper.SUBSCRIPTION_TYPE_NAME;

import com.google.common.base.CaseFormat;
import graphql.language.BooleanValue;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValue;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.IntValue;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.Value;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConfigurer;
import org.dotwebstack.framework.core.datafetchers.filter.FilterHelper;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.model.FieldArgument;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Subscription;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class TypeDefinitionRegistrySchemaFactory {

  private static final String GEOMETRY_TYPE = "Geometry";

  private static final String GEOMETRY_SRID_ARGUMENT_NAME = "srid";

  private static final String GEOMETRY_TYPE_ARGUMENT_NAME = "type";

  private static final String GEOMETRY_BBOX_ARGUMENT_NAME = "bbox";

  private static final String GEOMETRY_TYPE_ARGUMENT_TYPE = "GeometryType";

  private final Schema schema;

  private final Map<String, String> fieldFilterMap = new HashMap<>();

  public TypeDefinitionRegistrySchemaFactory(Schema schema, List<FilterConfigurer> filterConfigurers) {
    this.schema = schema;
    filterConfigurers.forEach(configurer -> configurer.configureFieldFilterMapping(fieldFilterMap));
  }

  public TypeDefinitionRegistry createTypeDefinitionRegistry() {
    var typeDefinitionRegistry = new TypeDefinitionRegistry();

    addEnumerations(typeDefinitionRegistry);
    addObjectTypes(typeDefinitionRegistry);
    addFilterTypes(typeDefinitionRegistry);
    addSortTypes(typeDefinitionRegistry);
    addContextTypes(typeDefinitionRegistry);
    addConnectionTypes(typeDefinitionRegistry);
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
            objectTypeDefinition.additionalData(IS_NESTED, TRUE.toString());
          }

          typeDefinitionRegistry.add(objectTypeDefinition.build());
        });
  }

  private void addFilterTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> typeDefinitionRegistry.add(createFilterObjectTypeDefinition(name, objectType)));
  }

  private void addSortTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> {
          if (!objectType.getSortableBy()
              .isEmpty()) {
            typeDefinitionRegistry.add(createSortableByObjectTypeDefinition(name, objectType.getSortableBy()));
          }
        });

    schema.getQueries()
        .forEach((name, query) -> {
          if (!query.getSortableBy()
              .isEmpty()) {
            typeDefinitionRegistry.add(createSortableByObjectTypeDefinition(name, query.getSortableBy()));
          }
        });
  }

  private void addContextTypes(TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getContexts()
        .entrySet()
        .stream()
        .filter(entry -> !entry.getValue()
            .getFields()
            .isEmpty())
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
            .defaultValue(getDefaultValue(entry))
            .build())
        .collect(Collectors.toList());

    return newInputObjectDefinition().name(formatContextTypeName(contextName))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private Value<?> getDefaultValue(Map.Entry<String, ContextField> entry) {
    String type = entry.getValue()
        .getType();
    if (GraphQLBoolean.getName()
        .equals(type)) {
      return BooleanValue.newBooleanValue(Boolean.parseBoolean(entry.getValue()
          .getDefaultValue()))
          .build();
    } else if (GraphQLInt.getName()
        .equals(type)) {
      return IntValue.newIntValue(new BigInteger(entry.getValue()
          .getDefaultValue()))
          .build();
    } else if (GraphQLFloat.getName()
        .equals(type)) {
      return FloatValue.newFloatValue(new BigDecimal(entry.getValue()
          .getDefaultValue()))
          .build();
    } else {
      return StringValue.newStringValue(entry.getValue()
          .getDefaultValue())
          .build();
    }
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
        .fieldDefinition(newFieldDefinition().name(NODES_FIELD_NAME)
            .type(newNonNullableListType(objectType.getName()))
            .additionalData(IS_PAGING_NODE, TRUE.toString())
            .build())
        .fieldDefinition(newFieldDefinition().name(OFFSET_FIELD_NAME)
            .type(newNonNullableType(GraphQLInt.getName()))
            .build())
        .additionalData(Map.of(IS_CONNECTION_TYPE, TRUE.toString()))
        .build();
  }

  private InputObjectTypeDefinition createFilterObjectTypeDefinition(String objectTypeName, ObjectType<?> objectType) {
    var filterName = createFilterName(objectTypeName);

    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();

    inputValueDefinitions.add(newInputValueDefinition().name("_exists")
        .type(newType("Boolean"))
        .build());

    inputValueDefinitions.addAll(objectType.getFilters()
        .entrySet()
        .stream()
        .map(entry -> newInputValueDefinition().name(entry.getKey())
            .type(newType(
                FilterHelper.getTypeNameForFilter(fieldFilterMap, objectType, entry.getKey(), entry.getValue())))
            .build())
        .collect(Collectors.toList()));

    inputValueDefinitions.add(newInputValueDefinition().name(OR_FIELD)
        .type(newType(filterName))
        .build());

    return newInputObjectDefinition().name(filterName)
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private EnumTypeDefinition createSortableByObjectTypeDefinition(String objectTypeName,
      Map<String, List<SortableByConfiguration>> sortableByConfig) {
    var orderName = createOrderName(objectTypeName);

    List<EnumValueDefinition> enumValueDefinitions = getEnumValueDefinitions(sortableByConfig);

    return newEnumTypeDefinition().name(orderName)
        .enumValueDefinitions(enumValueDefinitions)
        .build();
  }

  private List<EnumValueDefinition> getEnumValueDefinitions(
      Map<String, List<SortableByConfiguration>> sortableByConfig) {
    return sortableByConfig.keySet()
        .stream()
        .map(key -> newEnumValueDefinition().name(formatSortEnumName(key))
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
        type = NonNullType.newNonNullType(TypeUtils.newType(AGGREGATE_TYPE))
            .build();
      } else {
        return Optional.empty();
      }
    } else {
      type = createTypeForField(objectField);
    }

    Map<String, String> additionalData = new HashMap<>();
    if (isNotBlank(objectField.getValueFetcher())) {
      additionalData.put(CUSTOM_FIELD_VALUEFETCHER, objectField.getValueFetcher());
    }

    return Optional.of(newFieldDefinition().name(objectField.getName())
        .type(type)
        .inputValueDefinitions(createInputValueDefinitions(objectField))
        .additionalData(additionalData)
        .build());
  }

  private Type<?> createTypeForField(ObjectField objectField) {
    var type = objectField.getType();

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      return createListType(type, objectField.isPageable(), objectField.isNullable());
    }

    return createType(objectField);
  }

  private Type<?> createTypeForQuery(Query query) {
    var type = query.getType();

    Type<?> result;
    if (query.isList()) {
      result = createListType(type, query.isPageable(), false);
    } else {
      result = newType(query.getType());
    }

    if (query.isBatch()) {
      return ListType.newListType(result)
          .build();
    }

    return result;
  }

  private Type<?> createListType(String type, boolean pageable, boolean nullable) {
    if (pageable) {
      var connectionTypeName = createConnectionName(type);
      return newNonNullType(newType(connectionTypeName)).additionalData(IS_CONNECTION_TYPE, TRUE.toString())
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
      inputValueDefinitions.addAll(createGeometryArguments());
    }

    schema.getObjectType(objectField.getType())
        .ifPresent(objectType -> objectField.getKeys()
            .stream()
            .map(keyField -> createInputValueDefinition(keyField, objectType,
                Map.of(KEY_FIELD, keyField, KEY_PATH, keyField)))
            .forEach(inputValueDefinitions::add));

    objectField.getArguments()
        .stream()
        .map(this::createFieldInputValueDefinition)
        .forEach(inputValueDefinitions::add);

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      var objectType = schema.getObjectType(objectField.getType())
          .orElseThrow();

      createFilterArgument(objectField.getType(), objectType).ifPresent(inputValueDefinitions::add);

      createSortArgument(objectField.getType(), objectType.getSortableBy()).ifPresent(inputValueDefinitions::add);

      if (objectField.isPageable() && objectField.isList()) {
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
        .map(keyConfiguration -> createInputValueDefinition(keyConfiguration, objectType))
        .collect(Collectors.toList());

    createFilterArgument(subscription.getType(), objectType).ifPresent(inputValueDefinitions::add);

    createSortArgument(subscription, objectType).ifPresent(inputValueDefinitions::add);

    addOptionalContext(subscription.getContext(), inputValueDefinitions);

    return newFieldDefinition().name(queryName)
        .type(createType(subscription))
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }

  private FieldDefinition createQueryFieldDefinition(String queryName, Query query) {
    var objectType = schema.getObjectType(query.getType())
        .orElseThrow();

    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();

    inputValueDefinitions.addAll(createKeyArguments(query, objectType));
    inputValueDefinitions.addAll(createPagingArguments(query));
    createFilterArgument(query, objectType).ifPresent(inputValueDefinitions::add);

    createSortArgument(queryName, query, objectType).ifPresent(inputValueDefinitions::add);

    addOptionalContext(query.getContext(), inputValueDefinitions);

    return newFieldDefinition().name(queryName)
        .type(createTypeForQuery(query))
        .inputValueDefinitions(inputValueDefinitions)
        .additionalData(createQueryAdditionalData(query))
        .build();
  }

  private boolean hasContextWithFields(String context) {
    return schema.getContext(context)
        .map(Context::getFields)
        .filter(not(Map::isEmpty))
        .isPresent();
  }

  private Map<String, String> createQueryAdditionalData(Query query) {
    if (query.isBatch()) {
      return Map.of(IS_BATCH_KEY_QUERY, TRUE.toString());
    }

    return Map.of();
  }

  private List<InputValueDefinition> createKeyArguments(Query query, ObjectType<?> objectType) {
    return query.getKeyMap()
        .entrySet()
        .stream()
        .map(key -> {
          var aliasField = key.getKey();
          var keyField = getFieldKey(key.getValue());
          return createInputValueDefinition(aliasField, objectType,
              Map.of(KEY_FIELD, keyField, KEY_PATH, key.getValue()), query.isBatch());
        })
        .collect(Collectors.toList());
  }

  private Optional<InputValueDefinition> createFilterArgument(Query query, ObjectType<?> objectType) {
    if (query.isList()) {
      return createFilterArgument(query.getType(), objectType);
    }
    return Optional.empty();
  }

  private Optional<InputValueDefinition> createFilterArgument(String typeName, ObjectType<?> objectType) {
    if (!objectType.getFilters()
        .isEmpty()) {
      var filterName = createFilterName(typeName);

      var inputValueDefinition = newInputValueDefinition().name(FILTER_ARGUMENT_NAME)
          .type(newType(filterName))
          .build();

      return Optional.of(inputValueDefinition);
    }

    return Optional.empty();
  }

  private Optional<InputValueDefinition> createSortArgument(String queryName, Query query, ObjectType<?> objectType) {
    if (!query.isList()) {
      return Optional.empty();
    }
    if (query.getSortableBy()
        .isEmpty()) {
      return createSortArgument(query.getType(), objectType.getSortableBy());
    }
    return createSortArgument(queryName, query.getSortableBy());
  }

  private Optional<InputValueDefinition> createSortArgument(Subscription subscription, ObjectType<?> objectType) {
    return createSortArgument(subscription.getType(), objectType.getSortableBy());
  }

  private Optional<InputValueDefinition> createSortArgument(String typeName,
      Map<String, List<SortableByConfiguration>> sortableByConfig) {
    if (!sortableByConfig.isEmpty()) {
      var orderName = createOrderName(typeName);

      var firstArgument = sortableByConfig.keySet()
          .stream()
          .findFirst()
          .map(this::formatSortEnumName)
          .orElseThrow();

      var inputValueDefinition = newInputValueDefinition().name(SORT_ARGUMENT_NAME)
          .type(newType(orderName))
          .defaultValue(EnumValue.newEnumValue(firstArgument)
              .build())
          .build();

      return Optional.of(inputValueDefinition);
    }

    return Optional.empty();
  }

  private String formatSortEnumName(String enumName) {
    return CaseFormat.LOWER_CAMEL.to(UPPER_UNDERSCORE, enumName);
  }

  private String formatContextTypeName(String contextName) {
    return UPPER_UNDERSCORE.to(UPPER_CAMEL, String.format("%s_%s", contextName, CONTEXT_TYPE_SUFFIX));
  }

  private void addOptionalContext(String contextName, List<InputValueDefinition> inputValueDefinitions) {
    if (hasContextWithFields(contextName)) {
      inputValueDefinitions.add(newInputValueDefinition().name(CONTEXT_ARGUMENT_NAME)
          .type(newType(formatContextTypeName(contextName)))
          .defaultValue(ObjectValue.newObjectValue()
              .build())
          .build());
    }
  }

  private List<InputValueDefinition> createPagingArguments(Query query) {
    if (query.isList() && query.isPageable()) {
      return Stream.concat(createFirstArgument().stream(), createOffsetArgument().stream())
          .collect(Collectors.toList());
    }

    return List.of();
  }

  private Optional<InputValueDefinition> createFirstArgument() {
    return Optional.of(newInputValueDefinition().name(FIRST_ARGUMENT_NAME)
        .type(newType(GraphQLInt.getName()))
        .defaultValue(IntValue.newIntValue(FIRST_DEFAULT_VALUE)
            .build())
        .build());
  }

  private Optional<InputValueDefinition> createOffsetArgument() {
    return Optional.of(newInputValueDefinition().name(OFFSET_ARGUMENT_NAME)
        .type(newType(GraphQLInt.getName()))
        .defaultValue(IntValue.newIntValue(OFFSET_DEFAULT_VALUE)
            .build())
        .build());
  }

  private InputValueDefinition createInputValueDefinition(String keyPath, ObjectType<?> objectType) {
    return createInputValueDefinition(keyPath, objectType, Map.of());
  }

  private InputValueDefinition createInputValueDefinition(String keyPath, ObjectType<?> objectType,
      Map<String, String> additionalData) {
    return createInputValueDefinition(keyPath, objectType, additionalData, false);
  }

  private InputValueDefinition createInputValueDefinition(String aliasField, ObjectType<?> objectType,
      Map<String, String> additionalData, boolean batch) {

    var keyField = additionalData.get(KEY_FIELD);
    var keyPath = additionalData.get(KEY_PATH);
    var keyObjectType = getObjectType(objectType, keyPath);

    var fieldType = createType(keyField, keyObjectType);

    return newInputValueDefinition().name(aliasField)
        .type(batch ? ListType.newListType(fieldType)
            .build() : fieldType)
        .additionalData(additionalData)
        .build();
  }

  private InputValueDefinition createFieldInputValueDefinition(FieldArgument fieldArgument) {
    return newInputValueDefinition().name(fieldArgument.getName())
        .type(createType(fieldArgument))
        .build();
  }

  private List<InputValueDefinition> createGeometryArguments() {
    return List.of(createGeometrySridArgument(), createGeometryTypeArgument(), createGeometryBboxArgument());
  }

  private InputValueDefinition createGeometrySridArgument() {
    return newInputValueDefinition().name(GEOMETRY_SRID_ARGUMENT_NAME)
        .type(newType(GraphQLInt.getName()))
        .build();
  }

  private InputValueDefinition createGeometryTypeArgument() {
    return newInputValueDefinition().name(GEOMETRY_TYPE_ARGUMENT_NAME)
        .type(newType(GEOMETRY_TYPE_ARGUMENT_TYPE))
        .build();
  }

  private InputValueDefinition createGeometryBboxArgument() {
    return newInputValueDefinition().name(GEOMETRY_BBOX_ARGUMENT_NAME)
        .type(newType(GraphQLBoolean.getName()))
        .defaultValue(BooleanValue.of(false))
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
