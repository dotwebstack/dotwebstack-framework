package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterValidatorTest {

  private final SchemaReader dwsReader = new SchemaReader(TestHelper.createSimpleObjectMapper());

  private FilterValidator filterValidator;

  @BeforeEach
  void doBefore() {
    filterValidator = new FilterValidator();
  }

  @Test
  void validate_throwsNoException_withValidFilterFields() {
    var schema = dwsReader.read("validators/dotwebstack-with-valid-filter.yaml");
    filterValidator.validate(schema);
  }

  @Test
  void validate_throwsException_withInvalidFilterFields() {
    var schema = dwsReader.read("validators/dotwebstack-with-invalid-filter.yaml");

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(), is("Filter field 'invalid' not found in object type 'Address'."));
  }

  @Test
  void validate_throwsException_withInvalidCaseInsensitiveEnumerationFilterField() {
    var statusField = new TestObjectField();
    statusField.setType(Scalars.GraphQLString.getName());
    var enumStatus = new FieldEnumConfiguration();
    enumStatus.setType("status");
    enumStatus.setValues(List.of("active"));
    statusField.setEnumeration(enumStatus);

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("status", statusField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setType(FilterType.EXACT);
    filterConfiguration.setField("status");
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
    var intField = new TestObjectField();
    intField.setType(Scalars.GraphQLInt.getName());

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("intField", intField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setType(FilterType.EXACT);
    filterConfiguration.setField("intField");
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
    var identifierField = new TestObjectField();
    identifierField.setType(Scalars.GraphQLInt.getName());

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("identifier", identifierField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setField("identifier");
    filterConfiguration.setType(FilterType.PARTIAL);
    objectType.setFilters(Map.of("testFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    var thrown = assertThrows(InvalidConfigurationException.class, () -> filterValidator.validate(schema));

    assertThat(thrown.getMessage(), equalTo(
        "Filter 'testFilter' of type 'Partial' in object type 'testObject' doesn´t refer to a 'String' field type."));
  }

  @Test
  void validate_throwsNoException_withValidTermFieldTypeConfiguration() {
    var identifierField = new TestObjectField();
    identifierField.setType(Scalars.GraphQLInt.getName());

    var nameField = new TestObjectField();
    nameField.setType(Scalars.GraphQLString.getName());

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("identifier", identifierField, "name", nameField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setType(FilterType.PARTIAL);
    filterConfiguration.setField("name");
    objectType.setFilters(Map.of("testFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    filterValidator.validate(schema);
  }

}
