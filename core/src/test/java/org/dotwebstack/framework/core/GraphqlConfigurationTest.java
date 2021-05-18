package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.language.EnumTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeUtil;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.junit.jupiter.api.Test;

class GraphqlConfigurationTest {

  private final DotWebStackConfigurationReader dwsReader =
      new DotWebStackConfigurationReader(TypeConfigurationImpl.class, FieldConfigurationImpl.class);

  private final GraphqlConfiguration graphqlConfiguration = new GraphqlConfiguration();


  @Test
  void typeDefinitionRegistry_registerQueries_whenConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-queries.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Query")
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType("Query")
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is("Query"));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(2));

    var breweryFieldDefinition = fieldDefinitions.get(0);
    assertThat(breweryFieldDefinition.getName(), is("brewery"));
    assertNonNullType(breweryFieldDefinition.getType(), "Brewery");
    assertThat(breweryFieldDefinition.getInputValueDefinitions()
        .size(), is(1));

    var queryInputValueDefinition = breweryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(queryInputValueDefinition.getName(), is("identifier"));
    assertNonNullType(queryInputValueDefinition.getType(), "ID");

    var breweryCollectionFieldDefinition = fieldDefinitions.get(1);
    assertThat(breweryCollectionFieldDefinition.getName(), is("breweryCollection"));
    assertNonNullListType(breweryCollectionFieldDefinition.getType(), "Brewery");
    assertThat(breweryCollectionFieldDefinition.getInputValueDefinitions(), empty());
  }

  @Test
  void typeDefinitionRegistry_registerDummyQuery_whenNoQueriesConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-no-queries.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Query")
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType("Query")
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is("Query"));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    var dummyFieldDefinition = fieldDefinitions.get(0);
    assertThat(dummyFieldDefinition.getName(), is("dummy"));
    assertType(dummyFieldDefinition.getType(), "String");
    assertThat(dummyFieldDefinition.getInputValueDefinitions(), empty());
  }

  @Test
  void typeDefinitionRegistry_registerSubscriptions_whenConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-subscriptions.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Subscription")
        .isPresent(), is(true));
    var queryTypeDefinition = registry.getType("Subscription")
        .orElseThrow();
    assertThat(queryTypeDefinition.getName(), is("Subscription"));
    assertThat(queryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) queryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(1));

    var brewerySubscriptionFieldDefinition = fieldDefinitions.get(0);
    assertThat(brewerySubscriptionFieldDefinition.getName(), is("brewerySubscription"));
    assertNonNullType(brewerySubscriptionFieldDefinition.getType(), "Brewery");
    assertThat(brewerySubscriptionFieldDefinition.getInputValueDefinitions(), empty());
  }

  @Test
  void typeDefinitionRegistry_noSubscriptions_whenNoSubscriptionsConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-no-subscriptions.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertTrue(registry.getType("Subscription")
        .isEmpty());
  }

  @Test
  void typeDefinitionRegistry_registerEnumerations_whenConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-enumerations.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Taste")
        .isPresent(), is(true));
    var tasteTypeDefinition = registry.getType("Taste")
        .orElseThrow();
    assertThat(tasteTypeDefinition.getName(), is("Taste"));
    assertThat(tasteTypeDefinition, instanceOf(EnumTypeDefinition.class));
    var enumValueDefinitions = ((EnumTypeDefinition) tasteTypeDefinition).getEnumValueDefinitions()
        .stream()
        .map(valueDef -> valueDef.getName())
        .collect(Collectors.toList());
    assertThat(enumValueDefinitions, contains("MEATY", "FRUITY", "SMOKY", "SPICY", "WATERY"));
  }

  @Test
  void typeDefinitionRegistry_validationException_whenEnumerationHasNoValues() {
    assertThrows(InvalidConfigurationException.class, () -> {
      dwsReader.read("dotwebstack/dotwebstack-enumerations-empty-values.yaml");
    });
  }

  @Test
  void typeDefinitionRegistry_registerObjectTypesWithScalarFields_whenConfigured() {
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-objecttypes-scalar-fields.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

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
    var dotWebStackConfiguration = dwsReader.read("dotwebstack/dotwebstack-objecttypes-complex-fields.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Brewery")
        .isPresent(), is(true));
    var breweryTypeDefinition = registry.getType("Brewery")
        .orElseThrow();
    assertThat(breweryTypeDefinition.getName(), is("Brewery"));
    assertThat(breweryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) breweryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(6));

    var geometryFieldDefinition = fieldDefinitions.get(1);
    assertFieldDefinition(geometryFieldDefinition, "geometry", "Geometry", 1);

    var geometryInputValueDefinition = geometryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(geometryInputValueDefinition.getName(), is("type"));
    assertType(geometryInputValueDefinition.getType(), "GeometryType");

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

    var beersFieldDefinition = fieldDefinitions.get(4);
    assertThat(beersFieldDefinition.getName(), is("beers"));
    assertListType(beersFieldDefinition.getType(), "Beer");
    assertThat(beersFieldDefinition.getInputValueDefinitions(), empty());

    var beerAggFieldDefinition = fieldDefinitions.get(5);
    assertFieldDefinition(beerAggFieldDefinition, "beerAgg", "Aggregate");
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

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonTypeName("test")
  static class TypeConfigurationImpl extends AbstractTypeConfiguration<FieldConfigurationImpl> {

    @Override
    public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
      return null;
    }

    @Override
    public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
      return null;
    }

    @Override
    public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
      return null;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  static class FieldConfigurationImpl extends AbstractFieldConfiguration {
    public boolean isAggregate() {
      return AggregateHelper.isAggregate(this);
    }

    @Override
    public String getType() {
      if (isAggregate()) {
        return AggregateConstants.AGGREGATE_TYPE;
      }
      return super.getType();
    }

    @Override
    public boolean isScalarField() {
      return false;
    }

    @Override
    public boolean isObjectField() {
      return false;
    }

    @Override
    public boolean isNestedObjectField() {
      return false;
    }

    @Override
    public boolean isAggregateField(){
      return isAggregate();
    }
  }
}
