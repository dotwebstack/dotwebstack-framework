package org.dotwebstack.framework.core;

import static graphql.language.BooleanValue.newBooleanValue;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.FloatValue.newFloatValue;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.IntValue.newIntValue;
import static graphql.language.StringValue.newStringValue;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import graphql.language.BooleanValue;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValue;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeUtil;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConfigurer;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.testhelpers.TestBackendModule;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;

class TypeDefinitionRegistrySchemaFactoryTest {

  private static final String QUERY_TYPE_NAME = "Query";

  private SchemaReader schemaReader;

  private final FilterConfigurer filterConfigurer = fieldFilterMap -> {
    fieldFilterMap.put("String", FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put("Float", FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put("Int", FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE);
  };

  @BeforeEach
  void doBefore() {
    schemaReader = new SchemaReader(TestHelper.createSimpleObjectMapper());
  }


  @Test
  void typeDefinitionRegistry_registerQueries_whenConfigured() {
    var backendModule = new TestBackendModule(new TestBackendLoaderFactory(mock(DatabaseClient.class)));
    var testHelper = new TestHelper(backendModule);
    var dotWebStackConfiguration = testHelper.loadSchema("dotwebstack/dotwebstack-queries.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType(QUERY_TYPE_NAME)
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(3));

    var breweryFieldDefinition = fieldDefinitions.get(0);
    assertThat(breweryFieldDefinition.getName(), is("brewery"));
    assertType(breweryFieldDefinition.getType(), "Brewery");
    assertThat(breweryFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var breweryQueryIdentifierInputValueDefinition = breweryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(breweryQueryIdentifierInputValueDefinition.getName(), is("identifier"));
    assertNonNullType(breweryQueryIdentifierInputValueDefinition.getType(), "ID");

    var breweryCityFieldDefinition = fieldDefinitions.get(2);

    assertThat(breweryCityFieldDefinition.getInputValueDefinitions()
        .size(), is(2));
    var breweryCityqueryIdentifierInputValueDefinition = breweryCityFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(breweryCityqueryIdentifierInputValueDefinition.getName(), is("identifier"));
    assertNonNullType(breweryCityqueryIdentifierInputValueDefinition.getType(), "ID");

    var breweryCityqueryCityInputValueDefinition = breweryCityFieldDefinition.getInputValueDefinitions()
        .get(1);
    assertThat(breweryCityqueryCityInputValueDefinition.getName(), is("city"));
    assertNonNullType(breweryCityqueryCityInputValueDefinition.getType(), "String");

    var breweryCollectionFieldDefinition = fieldDefinitions.get(1);
    assertThat(breweryCollectionFieldDefinition.getName(), is("breweryCollection"));
    assertNonNullListType(breweryCollectionFieldDefinition.getType(), "Brewery");
    assertThat(breweryCollectionFieldDefinition.getInputValueDefinitions(), empty());
  }

  @Test
  void typeDefinitionRegistry_registerQueries_whenConfiguredWithPaging() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-queries-with-paging.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType(QUERY_TYPE_NAME)
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(2));

    var breweryFieldDefinition = fieldDefinitions.get(0);
    assertThat(breweryFieldDefinition.getName(), is("brewery"));
    assertType(breweryFieldDefinition.getType(), "Brewery");
    assertThat(breweryFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var queryInputValueDefinition = breweryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(queryInputValueDefinition.getName(), is("identifier"));
    assertNonNullType(queryInputValueDefinition.getType(), "ID");

    var breweryConnectionFieldDefinition = fieldDefinitions.get(1);
    assertThat(breweryConnectionFieldDefinition.getName(), is("breweryCollection"));
    assertThat(TypeHelper.getTypeName(breweryConnectionFieldDefinition.getType()), equalTo("BreweryConnection"));

    assertThat(breweryConnectionFieldDefinition.getInputValueDefinitions(),
        IsIterableContaining.hasItems(allOf(hasProperty("name", equalTo(PagingConstants.FIRST_ARGUMENT_NAME))),
            hasProperty("name", equalTo(PagingConstants.OFFSET_FIELD_NAME))));
  }

  @Test
  void typeDefinitionRegistry_registerQueriesWithFilters_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-queries-with-filters.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType(QUERY_TYPE_NAME)
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(2));

    var breweryFieldDefinition = fieldDefinitions.get(0);
    assertThat(breweryFieldDefinition.getName(), is("brewery"));
    assertType(breweryFieldDefinition.getType(), "Brewery");
    assertThat(breweryFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var queryInputValueDefinition = breweryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(queryInputValueDefinition.getName(), is("identifier"));
    assertNonNullType(queryInputValueDefinition.getType(), "ID");

    var breweryCollectionFieldDefinition = fieldDefinitions.get(1);
    assertThat(breweryCollectionFieldDefinition.getName(), is("breweryCollection"));
    assertNonNullListType(breweryCollectionFieldDefinition.getType(), "Brewery");

    assertThat(breweryCollectionFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var inputValueDefinition = breweryCollectionFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(inputValueDefinition.getName(), is("filter"));

    assertThat(inputValueDefinition.getType(), instanceOf(TypeName.class));
    assertThat(((TypeName) inputValueDefinition.getType()).getName(), equalTo("BreweryFilter"));

    var filterTypeDefinition = (InputObjectTypeDefinition) registry.getType(inputValueDefinition.getType())
        .orElse(null);

    assertThat(filterTypeDefinition, notNullValue());
    assertThat(filterTypeDefinition.getInputValueDefinitions()
        .size(), is(5));
    assertThat(filterTypeDefinition.getInputValueDefinitions()
        .stream()
        .map(InputValueDefinition::getName)
        .collect(Collectors.toList()),
        equalTo(List.of("_exists", "name", "abv", "inceptionYear", FilterConstants.OR_FIELD)));
    assertThat(filterTypeDefinition.getInputValueDefinitions()
        .stream()
        .map(InputValueDefinition::getType)
        .map(TypeName.class::cast)
        .map(TypeName::getName)
        .collect(Collectors.toList()),
        equalTo(List.of("Boolean", "StringFilter", "FloatFilter", "IntFilter", "BreweryFilter")));
  }

  @Test
  void typeDefinitionRegistry_registerQueriesWithSortableBy_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-queries-with-sortable-by.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType(QUERY_TYPE_NAME)
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    var breweryCollectionFieldDefinition = fieldDefinitions.get(0);
    assertThat(breweryCollectionFieldDefinition.getName(), is("breweryCollection"));
    assertNonNullListType(breweryCollectionFieldDefinition.getType(), "Brewery");

    assertThat(breweryCollectionFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var inputValueDefinition = breweryCollectionFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(inputValueDefinition.getName(), is("sort"));
    assertThat(inputValueDefinition.getDefaultValue(), instanceOf(EnumValue.class));
    assertThat(((EnumValue) inputValueDefinition.getDefaultValue()).getName(), is("NAME"));

    assertThat(inputValueDefinition.getType(), instanceOf(TypeName.class));
    assertThat(((TypeName) inputValueDefinition.getType()).getName(), equalTo("BreweryOrder"));

    var sortTypeDefinition = (EnumTypeDefinition) registry.getType(inputValueDefinition.getType())
        .orElse(null);

    assertThat(sortTypeDefinition, notNullValue());
    assertThat(sortTypeDefinition.getEnumValueDefinitions()
        .size(), is(2));
    assertThat(sortTypeDefinition.getEnumValueDefinitions()
        .stream()
        .map(EnumValueDefinition::getName)
        .collect(Collectors.toList()), equalTo(List.of("NAME", "ADDRESS")));
  }

  @Test
  void typeDefinitionRegistry_registerDummyQuery_whenNoQueriesConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-no-queries.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType(QUERY_TYPE_NAME)
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));
  }

  @Test
  void typeDefinitionRegistry_registerSubscriptions_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-subscriptions.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Subscription")
        .isPresent(), is(true));
    var subscriptionTypeDefinition = registry.getType("Subscription")
        .orElseThrow();
    assertThat(subscriptionTypeDefinition.getName(), is("Subscription"));
    assertThat(subscriptionTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) subscriptionTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    var brewerySubscriptionFieldDefinition = fieldDefinitions.get(0);
    assertThat(brewerySubscriptionFieldDefinition.getName(), is("brewerySubscription"));
    assertNonNullType(brewerySubscriptionFieldDefinition.getType(), "Brewery");
    assertThat(brewerySubscriptionFieldDefinition.getInputValueDefinitions(), empty());
  }

  @Test
  void typeDefinitionRegistry_registerSubscriptionsWithSortableBy_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-subscriptions-with-sortable-by.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Subscription")
        .isPresent(), is(true));
    var subscriptionTypeDefinition = registry.getType("Subscription")
        .orElseThrow();
    assertThat(subscriptionTypeDefinition.getName(), is("Subscription"));
    assertThat(subscriptionTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) subscriptionTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    var brewerySubscriptionFieldDefinition = fieldDefinitions.get(0);
    assertThat(brewerySubscriptionFieldDefinition.getName(), is("brewerySubscription"));
    assertNonNullType(brewerySubscriptionFieldDefinition.getType(), "Brewery");

    assertThat(brewerySubscriptionFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var inputValueDefinition = brewerySubscriptionFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(inputValueDefinition.getName(), is("sort"));
    assertThat(inputValueDefinition.getDefaultValue(), instanceOf(EnumValue.class));
    assertThat(((EnumValue) inputValueDefinition.getDefaultValue()).getName(), is("NAME"));

    assertThat(inputValueDefinition.getType(), instanceOf(TypeName.class));
    assertThat(((TypeName) inputValueDefinition.getType()).getName(), equalTo("BreweryOrder"));

    var sortTypeDefinition = (EnumTypeDefinition) registry.getType(inputValueDefinition.getType())
        .orElse(null);

    assertThat(sortTypeDefinition, notNullValue());
    assertThat(sortTypeDefinition.getEnumValueDefinitions()
        .size(), is(2));
    assertThat(sortTypeDefinition.getEnumValueDefinitions()
        .stream()
        .map(EnumValueDefinition::getName)
        .collect(Collectors.toList()), equalTo(List.of("NAME", "ADDRESS")));
  }

  @Test
  void typeDefinitionRegistry_noSubscriptions_whenNoSubscriptionsConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-no-subscriptions.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertTrue(registry.getType("Subscription")
        .isEmpty());
  }

  @Test
  void typeDefinitionRegistry_registerEnumerations_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-enumerations.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Taste")
        .isPresent(), is(true));
    var tasteTypeDefinition = registry.getType("Taste")
        .orElseThrow();
    assertThat(tasteTypeDefinition.getName(), is("Taste"));
    assertThat(tasteTypeDefinition, instanceOf(EnumTypeDefinition.class));
    var enumValueDefinitions = ((EnumTypeDefinition) tasteTypeDefinition).getEnumValueDefinitions()
        .stream()
        .map(EnumValueDefinition::getName)
        .collect(Collectors.toList());
    assertThat(enumValueDefinitions, contains("MEATY", "FRUITY", "SMOKY", "SPICY", "WATERY"));
  }

  @Test
  void typeDefinitionRegistry_registerObjectTypesWithScalarFields_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-objecttypes-scalar-fields.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Brewery")
        .isPresent(), is(true));
    var breweryTypeDefinition = registry.getType("Brewery")
        .orElseThrow();
    assertThat(breweryTypeDefinition.getName(), is("Brewery"));
    assertThat(breweryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) breweryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(6));

    var identifierFieldDefinition = fieldDefinitions.get(0);
    assertFieldDefinition(identifierFieldDefinition, "identifier", "ID");

    var nameFieldDefinition = fieldDefinitions.get(1);
    assertFieldDefinition(nameFieldDefinition, "name", "String");

    var statusFieldDefinition = fieldDefinitions.get(2);
    assertFieldDefinition(statusFieldDefinition, "status", "Status");

    var nrOfEmplFieldDefinition = fieldDefinitions.get(3);
    assertFieldDefinition(nrOfEmplFieldDefinition, "numberOfEmpoyees", "Int");

    var revenueFieldDefinition = fieldDefinitions.get(4);
    assertFieldDefinition(revenueFieldDefinition, "revenue", "Float");

    var regDateFieldDefinition = fieldDefinitions.get(5);
    assertFieldDefinition(regDateFieldDefinition, "registrationDate", "Date");
  }

  @Test
  void typeDefinitionRegistry_registerObjectTypesWithComplexFields_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-objecttypes-complex-fields.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Brewery")
        .isPresent(), is(true));
    var breweryTypeDefinition = registry.getType("Brewery")
        .orElseThrow();
    assertThat(breweryTypeDefinition.getName(), is("Brewery"));
    assertThat(breweryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) breweryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(7));

    assertGeometryFieldDefinition(fieldDefinitions.get(1));

    var addressesFieldDefinition = fieldDefinitions.get(2);
    assertThat(addressesFieldDefinition.getName(), is("addresses"));
    assertListType(addressesFieldDefinition.getType(), "Address");
    assertThat(addressesFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var addressTypeInputValueDefinition = addressesFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(addressTypeInputValueDefinition.getName(), is("addressType"));
    assertListType(addressTypeInputValueDefinition.getType(), "String");

    var visitAddressFieldDefinition = fieldDefinitions.get(3);
    assertThat(visitAddressFieldDefinition.getName(), is("visitAddress"));
    assertType(visitAddressFieldDefinition.getType(), "Address");
    assertThat(visitAddressFieldDefinition.getInputValueDefinitions(), empty());

    var beerFieldDefinition = fieldDefinitions.get(4);
    assertThat(beerFieldDefinition.getName(), is("beer"));
    assertType(beerFieldDefinition.getType(), "Beer");

    var beersFieldDefinition = fieldDefinitions.get(5);
    assertThat(beersFieldDefinition.getName(), is("beers"));
    assertListType(beersFieldDefinition.getType(), "Beer");
    assertThat(beersFieldDefinition.getInputValueDefinitions(), empty());

    var beerAggFieldDefinition = fieldDefinitions.get(6);
    assertFieldDefinition(beerAggFieldDefinition, "beerAgg", "Aggregate");
  }

  private void assertGeometryFieldDefinition(FieldDefinition geometryFieldDefinition) {
    assertFieldDefinition(geometryFieldDefinition, "geometry", "Geometry", 3);

    var geometrySridInputValueDefinition = geometryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(geometrySridInputValueDefinition.getName(), is("srid"));
    assertType(geometrySridInputValueDefinition.getType(), "Int");

    var geometryTypeInputValueDefinition = geometryFieldDefinition.getInputValueDefinitions()
        .get(1);
    assertThat(geometryTypeInputValueDefinition.getName(), is("type"));
    assertType(geometryTypeInputValueDefinition.getType(), "GeometryType");

    var geometryBboxInputValueDefinition = geometryFieldDefinition.getInputValueDefinitions()
        .get(2);
    assertThat(geometryBboxInputValueDefinition.getName(), is("bbox"));
    assertType(geometryBboxInputValueDefinition.getType(), "Boolean");
    assertThat(geometryBboxInputValueDefinition.getDefaultValue(), instanceOf(BooleanValue.class));
    assertThat(((BooleanValue) geometryBboxInputValueDefinition.getDefaultValue()).isValue(), is(false));
  }

  @Test
  void typeDefinitionRegistry_registerContext_whenConfigured() {
    var dotWebStackConfiguration = schemaReader.read("dotwebstack/dotwebstack-context.yaml");

    var registry = new TypeDefinitionRegistrySchemaFactory(dotWebStackConfiguration, List.of(filterConfigurer))
        .createTypeDefinitionRegistry();

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("HistoryContext")
        .map(Object::toString)
        .orElseThrow(),
        equalTo(newInputObjectDefinition().name("HistoryContext")
            .inputValueDefinition(newInputValueDefinition().name("validOn")
                .type(newNonNullableType("Date"))
                .defaultValue(newStringValue("NOW").build())
                .build())
            .inputValueDefinition(newInputValueDefinition().name("availableOn")
                .type(newNonNullableType("DateTime"))
                .defaultValue(newStringValue("NOW").build())
                .build())
            .inputValueDefinition(newInputValueDefinition().name("isAvailable")
                .type(newNonNullableType("Boolean"))
                .defaultValue(newBooleanValue(true).build())
                .build())
            .inputValueDefinition(newInputValueDefinition().name("numberOfEmployees")
                .type(newNonNullableType("Int"))
                .defaultValue(newIntValue(new BigInteger("1")).build())
                .build())
            .inputValueDefinition(newInputValueDefinition().name("pricePerBeer")
                .type(newNonNullableType("Float"))
                .defaultValue(newFloatValue(new BigDecimal("1.5")).build())
                .build())
            .build()
            .toString()));

    var queryTypeDefinition = registry.getType(QUERY_TYPE_NAME)
        .orElseThrow();

    assertThat(queryTypeDefinition.getName(), is(QUERY_TYPE_NAME));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));

    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    assertThat(fieldDefinitions.get(0)
        .toString(),
        equalTo(newFieldDefinition().name("breweryCollection")
            .type(newNonNullableListType("Brewery"))
            .inputValueDefinition(newInputValueDefinition().name("context")
                .type(newType("HistoryContext"))
                .defaultValue(ObjectValue.newObjectValue()
                    .build())
                .build())
            .build()
            .toString()));
  }

  @Test
  void typeDefinitionRegistry_addNestedKeyArguments_whenKeysAreConfigured() {
    var childTypeName = "Child";

    var childObjectType = new TestObjectType();
    childObjectType.setName(childTypeName);

    var childIdentifier = new TestObjectField();
    childIdentifier.setName("childIdentifier");
    childIdentifier.setType("String");
    childObjectType.setFields(Map.of("childIdentifier", childIdentifier));

    var parentTypeName = "Parent";
    var parentObjectType = new TestObjectType();
    parentObjectType.setName(parentTypeName);

    var childObjectField = new TestObjectField();
    childObjectField.setName("child");
    childObjectField.setType(childTypeName);
    childObjectField.setKeys(List.of("childIdentifier"));

    var parentIdentifier = new TestObjectField();
    parentIdentifier.setName("parentIdentifier");
    parentObjectType.setFields(Map.of("parentIdentifier", parentIdentifier, "child", childObjectField));

    var schema = new Schema();

    schema.getObjectTypes()
        .put(parentTypeName, parentObjectType);
    schema.getObjectTypes()
        .put(childTypeName, childObjectType);

    var query = new Query();
    query.setType(parentTypeName);
    query.setKeys(List.of("parentIdentifier"));

    schema.getQueries()
        .put("queryParent", query);

    var schemaFactory = new TypeDefinitionRegistrySchemaFactory(schema, List.of());

    var typeDefinitionRegistry = schemaFactory.createTypeDefinitionRegistry();

    assertThat(typeDefinitionRegistry, notNullValue());

    var actualParent = typeDefinitionRegistry.getType(parentTypeName)
        .orElse(null);
    assertThat(actualParent, notNullValue());
    assertThat(actualParent, instanceOf(ObjectTypeDefinition.class));
    assertThat(((ObjectTypeDefinition) actualParent).getFieldDefinitions(),
        hasItem(hasProperty("name", equalTo("child"))));

    var actualChild = ((ObjectTypeDefinition) actualParent).getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals("child"))
        .findFirst()
        .orElseThrow();
    assertThat(actualChild.getInputValueDefinitions(), hasItem(hasProperty("name", equalTo("childIdentifier"))));
  }

  private static void assertFieldDefinition(FieldDefinition fieldDefinition, String name, String type,
      int nrOfInputValueDefinitions) {
    assertThat(fieldDefinition.getName(), is(name));
    assertNonNullType(fieldDefinition.getType(), type);
    assertThat(fieldDefinition.getInputValueDefinitions()
        .size(), is(nrOfInputValueDefinitions));
  }

  private static void assertFieldDefinition(FieldDefinition fieldDefinition, String name, String type) {
    assertFieldDefinition(fieldDefinition, name, type, 0);
  }

  private static void assertType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(TypeName.class));
    assertThat(((TypeName) type).getName(), is(typeName));
  }

  private static void assertNonNullType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(NonNullType.class));
    assertType(TypeUtil.unwrapOne(type), typeName);
  }

  private static void assertListType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(ListType.class));
    assertNonNullType(TypeUtil.unwrapOne(type), typeName);
  }

  private static void assertNonNullListType(Type<?> type, String typeName) {
    assertThat(type, instanceOf(NonNullType.class));
    assertListType(TypeUtil.unwrapOne(type), typeName);
  }
}
