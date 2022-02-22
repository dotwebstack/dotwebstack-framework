package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QueryValidatorTest {

  private final SchemaReader dwsReader = new SchemaReader(TestHelper.createSimpleObjectMapper());

  @Test
  void validate_throwsException_forBatchQueryWithPaging() {
    var schema = new Schema();

    var query = new Query();
    query.setPageable(true);
    query.setBatch(true);

    schema.getQueries()
        .put("testQuery", query);

    var validator = new QueryValidator();

    var exception = assertThrows(InvalidConfigurationException.class, () -> validator.validate(schema));

    assertThat(exception.getMessage(),
        is("Batching for query 'testQuery' in combination with paging is not supported!"));
  }

  @Test
  void validate_throwsException_forBatchQueryWithoutKeys() {
    var schema = new Schema();

    var query = new Query();
    query.setBatch(true);

    schema.getQueries()
        .put("testQuery", query);

    var validator = new QueryValidator();

    var exception = assertThrows(InvalidConfigurationException.class, () -> validator.validate(schema));

    assertThat(exception.getMessage(), is("Batching for query 'testQuery' without keys is not possible!"));
  }

  @Test
  void validate_throwsException_forBatchQueryWithCompositeKey() {
    var schema = new Schema();

    var query = new Query();
    query.setBatch(true);
    query.setKeys(List.of("foo", "bar"));

    schema.getQueries()
        .put("testQuery", query);

    var validator = new QueryValidator();

    var exception = assertThrows(InvalidConfigurationException.class, () -> validator.validate(schema));

    assertThat(exception.getMessage(), is("Batching for query 'testQuery' with a composite key is not supported!"));
  }

  @Test
  void validate_throwsNoException_withValidKeyFields() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-valid-key.yaml");
    new QueryValidator().validate(dotWebStackConfiguration);
  }

  @ParameterizedTest
  @CsvSource(
      value = {
          "dotwebstack-with-invalid-nullable-objectfield-for-key.yaml | "
              + "A key can't contain fields that are nullable, for field: 'postalAddress'.",
          "dotwebstack-with-invalid-list-objectfield-for-key.yaml | "
              + "A key can't contain fields that are a list, for field: 'postalAddress'.",
          "dotwebstack-with-invalid-nullable-keyfield.yaml | "
              + "A key can't contain fields that are nullable, for field: 'city'.",
          "dotwebstack-with-invalid-key.yaml | "
              + "Key field 'zipcode' in object type 'Address' can't be resolved to a single scalar type.",
          "dotwebstack-with-invalid-type-for-key.yaml | "
              + "The type 'Beer', of query: 'breweryAddress', doesn't exist in the configuration."},
      delimiterString = "|")
  void validate_throwsException_forInvalidKeyConfiguration(String file, String message) {
    var dotWebStackConfiguration = dwsReader.read("validators/" + file);
    var validator = new QueryValidator();

    var exception =
        assertThrows(InvalidConfigurationException.class, () -> validator.validate(dotWebStackConfiguration));

    assertThat(exception.getMessage(), is(message));
  }
}
