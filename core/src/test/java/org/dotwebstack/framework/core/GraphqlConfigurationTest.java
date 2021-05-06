package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import graphql.language.EnumTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeUtil;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class GraphqlConfigurationTest {

  private final GraphqlConfiguration graphqlConfiguration = new GraphqlConfiguration();

  @Test
  void typeDefinitionRegistry_registerQueries_whenConfigured() {
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-queries.yaml");

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
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-no-queries.yaml");

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
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-subscriptions.yaml");

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
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-no-subscriptions.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertTrue(registry.getType("Subscription")
        .isEmpty());
  }

  @Test
  void typeDefinitionRegistry_registerEnumerations_whenConfigured() {
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-enumerations.yaml");

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
      readDotWebStackConfiguration("dotwebstack/dotwebstack-enumerations-empty-values.yaml");
    });
  }

  @Test
  void typeDefinitionRegistry_registerObjectTypes_whenConfigured() {
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-objecttypes.yaml");

    var registry = graphqlConfiguration.typeDefinitionRegistry(dotWebStackConfiguration);

    assertThat(registry, is(notNullValue()));
    assertThat(registry.getType("Brewery")
        .isPresent(), is(true));
    var breweryTypeDefinition = registry.getType("Brewery")
        .orElseThrow();
    assertThat(breweryTypeDefinition.getName(), is("Brewery"));
    assertThat(breweryTypeDefinition, instanceOf(ObjectTypeDefinition.class));
    var fieldDefinitions = ((ObjectTypeDefinition) breweryTypeDefinition).getFieldDefinitions();
    assertThat(fieldDefinitions.size(), is(10));

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

    var geometryFieldDefinition = fieldDefinitions.get(6);
    assertFieldDefinition(geometryFieldDefinition, "geometry", "Geometry", 1);

    var geometryInputValueDefinition = geometryFieldDefinition.getInputValueDefinitions()
        .get(0);
    assertThat(geometryInputValueDefinition.getName(), is("type"));
    assertType(geometryInputValueDefinition.getType(), "GeometryType");

    var addressFieldDefinition = fieldDefinitions.get(7);
    assertThat(addressFieldDefinition.getName(), is("address"));
    assertType(addressFieldDefinition.getType(), "Address");
    assertThat(addressFieldDefinition.getInputValueDefinitions(), empty());

    var beersFieldDefinition = fieldDefinitions.get(8);
    assertThat(beersFieldDefinition.getName(), is("beers"));
    assertListType(beersFieldDefinition.getType(), "Beer");
    assertThat(beersFieldDefinition.getInputValueDefinitions(), empty());

    var beerAggFieldDefinition = fieldDefinitions.get(9);
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

  private DotWebStackConfiguration readDotWebStackConfiguration(String filename) {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));
    var objectMapper = new ObjectMapper(new YAMLFactory());

    objectMapper.registerSubtypes(TypeConfigurationImpl.class, FieldConfigurationImpl.class);

    return ResourceLoaderUtils.getResource(filename)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getInputStream(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw new InvalidConfigurationException("Error while reading config file.", e);
          }
        })
        .map(configuration -> {
          Set<ConstraintViolation<DotWebStackConfiguration>> violations = Validation.buildDefaultValidatorFactory()
              .getValidator()
              .validate(configuration);

          if (!violations.isEmpty()) {
            throw invalidConfigurationException("Config file contains validation errors: {}", violations);
          }

          return configuration;
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", filename));
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
  }
}
