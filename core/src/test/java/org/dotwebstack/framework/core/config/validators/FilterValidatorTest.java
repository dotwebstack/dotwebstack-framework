package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;

class FilterValidatorTest {

  private final SchemaReader dwsReader = new SchemaReader(TestHelper.createSimpleObjectMapper());

  @Test
  void validate_throwsNoException_withValidFilterFields() {
    var schema = dwsReader.read("validators/dotwebstack-with-valid-filter.yaml");
    new FilterValidator().validate(schema);
  }

  @Test
  void validate_throwsException_withInvalidFilterFields() {
    var schema = dwsReader.read("validators/dotwebstack-with-invalid-filter.yaml");

    var thrown = assertThrows(InvalidConfigurationException.class, () -> new FilterValidator().validate(schema));

    assertThat(thrown.getMessage(),
        is("Filter field 'visitAddress.invalid' in object type 'Brewery' can't be resolved to a single scalar type."));
  }

  @Test
  void validate_throwsException_withInvalidTermFieldTypeConfiguration() {
    var identifierField = new TestObjectField();
    identifierField.setType(Scalars.GraphQLInt.getName());

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("identifier", identifierField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setField("identifier");
    filterConfiguration.setType(FilterType.TERM);
    objectType.setFilters(Map.of("testFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    var thrown = assertThrows(InvalidConfigurationException.class, () -> new FilterValidator().validate(schema));

    assertThat(thrown.getMessage(), equalTo(
        "Filter 'testFilter' of type 'Term' in object type 'testObject' doesn´t refer to an 'String' field type."));
  }

  @Test
  void validate_throwsNoException_withValidTermFieldTypeConfiguration() {
    var identifierField = new TestObjectField();
    identifierField.setType(Scalars.GraphQLInt.getName());

    var nameField = new TestObjectField();
    identifierField.setType(Scalars.GraphQLInt.getName());

    var objectType = new TestObjectType();
    objectType.setFields(Map.of("identifier", identifierField, "name", nameField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setType(FilterType.TERM);
    filterConfiguration.setField("name");
    objectType.setFilters(Map.of("testFilter", filterConfiguration));

    var schema = new Schema();
    schema.setObjectTypes(Map.of("testObject", objectType));

    new FilterValidator().validate(schema);
  }

}
