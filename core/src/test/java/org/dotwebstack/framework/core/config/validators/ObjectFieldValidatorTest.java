package org.dotwebstack.framework.core.config.validators;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ObjectFieldValidatorTest {

  @Test
  void validate_throwsNoException_withValidValueFetcher() {
    var valueFetcher = "shortname-valuefetcher";
    var schema = createSchema(valueFetcher, "String");

    var customValueFetcherDispatcher = mockValueFetcherDispatcher(valueFetcher);

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    objectFieldValidator.validate(schema);
  }

  @Test
  void validate_throwsNoException_withNoValueFetcher() {
    var schema = createSchema(null, "String");

    var customValueFetcherDispatcher = mockValueFetcherDispatcher(null);

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    objectFieldValidator.validate(schema);
  }

  @Test
  void validate_throwsException_withInvalidValueFetcher() {
    var schema = createSchema("shortname-valuefetcher-invalid", "String");

    var customValueFetcherDispatcher = mockValueFetcherDispatcher("shortname-valuefetcher");

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    var thrown = assertThrows(InvalidConfigurationException.class, () -> objectFieldValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("ValueFetcher 'shortname-valuefetcher-invalid' is not supported for field Brewery.shortName!"));
  }

  @Test
  void validate_throwsException_withNoCustomValueFetcherDispatcher() {
    var schema = createSchema("shortname-valuefetcher", "String");

    var objectFieldValidator = new ObjectFieldValidator(null);

    var thrown = assertThrows(InvalidConfigurationException.class, () -> objectFieldValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("ValueFetcher 'shortname-valuefetcher' is not supported for field Brewery.shortName!"));
  }

  @Test
  void validate_throwsException_withDifferentType() {
    var valueFetcher = "shortname-valuefetcher";
    var schema = createSchema(valueFetcher, "Integer");

    var customValueFetcherDispatcher = mockValueFetcherDispatcher(valueFetcher);

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    var thrown = assertThrows(InvalidConfigurationException.class, () -> objectFieldValidator.validate(schema));

    assertThat(thrown.getMessage(), is("Valuefetcher 'shortname-valuefetcher' configured with type 'Integer' but "
        + "implementation type is 'java.lang.String'!"));
  }

  private CustomValueFetcherDispatcher mockValueFetcherDispatcher(String valueFetcher) {
    var customValueFetcherDispatcher = mock(CustomValueFetcherDispatcher.class);
    when(customValueFetcherDispatcher.supports(valueFetcher)).thenReturn(true);
    Mockito.<Class<?>>when(customValueFetcherDispatcher.getResultType(valueFetcher))
        .thenReturn(String.class);
    return customValueFetcherDispatcher;
  }

  private Schema createSchema(String valueFetcher, String type) {
    var objectField = new TestObjectField();
    objectField.setValueFetcher(valueFetcher);
    objectField.setType(type);
    objectField.setName("shortName");

    var objectType = new TestObjectType();
    objectType.setName("Brewery");

    var schema = new Schema();

    objectType.setFields(Map.of("shortName", objectField));
    schema.setObjectTypes(Map.of("Brewery", objectType));

    return schema;
  }
}
