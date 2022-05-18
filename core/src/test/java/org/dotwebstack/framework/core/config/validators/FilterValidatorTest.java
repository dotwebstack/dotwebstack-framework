package org.dotwebstack.framework.core.config.validators;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.GraphQLScalarType;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterValidatorTest {

  private FilterValidator filterValidator;

  @BeforeEach
  void doBefore() {
    filterValidator = new FilterValidator();
  }

  @Test
  void validate_throwsNoException_withValidFilterFields() {
    Schema schema = createSchema("name");

    assertDoesNotThrow(() -> filterValidator.validate(schema));
  }

  @Test
  void validate_throwsException_withInvalidFilterField() {
    Schema schema = createSchema("invalid");

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(), is("Filter field 'invalid' not found in object type 'Brewery'."));
  }

  private Schema createSchema(String fieldName) {
    var objectType = new TestObjectType();

    var nameField = createObjectField(GraphQLString, objectType);

    objectType.setFields(Map.of("name", nameField));

    var filterConfiguration = createFilterConfiguration(fieldName);
    objectType.setFilters(Map.of(String.format("%sFilter", fieldName), filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("Brewery", objectType));
    return schema;
  }

  @Test
  void validate_throwsException_withInvalidCaseInsensitiveEnumerationFilterField() {
    var objectType = new TestObjectType();

    var statusField = createObjectField(GraphQLString, objectType);

    var enumStatus = new FieldEnumConfiguration();
    enumStatus.setType("status");
    enumStatus.setValues(List.of("active"));
    statusField.setEnumeration(enumStatus);

    objectType.setFields(Map.of("status", statusField));

    var filterConfiguration = createFilterConfiguration("status");
    filterConfiguration.setCaseSensitive(false);
    objectType.setFilters(Map.of("statusFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("Brewery", objectType));

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("Filter 'statusFilter' with property 'caseSensitive' is 'false' not valid for enumerations."));
  }

  @Test
  void validate_throwsException_withInvalidCaseInsensitiveIntegerFilterField() {
    var objectType = new TestObjectType();

    var intField = createObjectField(GraphQLInt, objectType);

    objectType.setFields(Map.of("int", intField));

    var filterConfiguration = createFilterConfiguration("int");
    filterConfiguration.setCaseSensitive(false);
    objectType.setFilters(Map.of("intFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("Brewery", objectType));

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("Filter 'intFilter' with property 'caseSensitive' is 'false' not valid for type 'Int'."));
  }

  @Test
  void validate_throwsException_withInvalidPartialFieldTypeConfiguration() {
    var objectType = new TestObjectType();

    var identifierField = createObjectField(GraphQLInt, objectType);

    objectType.setFields(Map.of("identifier", identifierField));

    var filterConfiguration = createFilterConfiguration("identifier", FilterType.PARTIAL, null);
    objectType.setFilters(Map.of("identifierFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(),
        equalTo("Filter 'identifierFilter' of type 'Partial' doesn´t refer to a 'String' field type."));
  }

  @Test
  void validate_throwsNoException_withValidTermFieldTypeConfiguration() {
    var objectType = new TestObjectType();

    var identifierField = createObjectField(GraphQLInt, objectType);
    var nameField = createObjectField(GraphQLString, objectType);

    objectType.setFields(Map.of("identifier", identifierField, "name", nameField));

    var filterConfiguration = createFilterConfiguration("name", FilterType.PARTIAL, null);
    objectType.setFilters(Map.of("testFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    assertDoesNotThrow(() -> filterValidator.validate(schema));
  }

  @Test
  void validate_throwsNoException_withDependsOnConfiguration() {
    Schema schema = createDependsOnSchema("houseNumberFilter");

    assertDoesNotThrow(() -> filterValidator.validate(schema));
  }

  @Test
  void validate_throwsException_withNonExistingDependsOn() {
    Schema schema = createDependsOnSchema("nonExistingFilter");

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(),
        equalTo("Filter 'houseNumberAdditionFilter' depends on non existing filter 'nonExistingFilter'."));
  }

  @Test
  void validate_throwsException_withReferToOneselfDependsOn() {
    Schema schema = createDependsOnSchema("houseNumberAdditionFilter");

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(), equalTo("Filter 'houseNumberAdditionFilter' can't refer to oneself."));
  }

  private Schema createDependsOnSchema(String lastDependsOn) {
    var objectType = new TestObjectType();

    var postalField = createObjectField(GraphQLString, objectType);
    var houseNumberField = createObjectField(GraphQLString, objectType);
    var houseNumberAdditionField = createObjectField(GraphQLString, objectType);

    objectType.setFields(Map.of("postal", postalField, "houseNumber", houseNumberField, "houseNumberAddition",
        houseNumberAdditionField));

    var filterConfigurationPostal = createFilterConfiguration("postal");
    var filterConfigurationHouseNumber = createFilterConfiguration("houseNumber", null, "postalFilter");
    var filterConfigurationHouseNumberAddition = createFilterConfiguration("houseNumberAddition", null, lastDependsOn);

    objectType.setFilters(Map.of("postalFilter", filterConfigurationPostal, "houseNumberFilter",
        filterConfigurationHouseNumber, "houseNumberAdditionFilter", filterConfigurationHouseNumberAddition));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("Brewery", objectType));
    return schema;
  }

  private FilterConfiguration createFilterConfiguration(String fieldName) {
    return createFilterConfiguration(fieldName, null, null);
  }

  private FilterConfiguration createFilterConfiguration(String fieldName, FilterType filterType, String dependsOn) {
    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setName(String.format("%sFilter", fieldName));
    filterConfiguration.setField(fieldName);
    filterConfiguration.setDependsOn(dependsOn);
    filterConfiguration.setType(filterType == null ? FilterType.EXACT : FilterType.PARTIAL);
    return filterConfiguration;
  }

  private TestObjectField createObjectField(GraphQLScalarType type, TestObjectType objectType) {
    var postalField = new TestObjectField();
    postalField.setType(type.getName());
    postalField.setObjectType(objectType);
    return postalField;
  }

}
