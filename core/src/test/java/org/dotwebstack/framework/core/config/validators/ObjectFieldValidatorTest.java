package org.dotwebstack.framework.core.config.validators;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.testhelpers.TestHelper;
import org.junit.jupiter.api.Test;

class ObjectFieldValidatorTest {

  private final SchemaReader dwsReader = new SchemaReader(TestHelper.createSimpleObjectMapper());

  @Test
  void validate_throwsNoException_withValidValueFetcher() {
    var schema = dwsReader.read("validators/dotwebstack-with-valid-value-fetcher.yaml");

    var customValueFetcherDispatcher = mock(CustomValueFetcherDispatcher.class);
    when(customValueFetcherDispatcher.supports("shortname-valuefetcher")).thenReturn(true);

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    objectFieldValidator.validate(schema);
  }

  @Test
  void validate_throwsException_withInvalidValueFetcher() {
    var schema = dwsReader.read("validators/dotwebstack-with-invalid-value-fetcher.yaml");

    var customValueFetcherDispatcher = mock(CustomValueFetcherDispatcher.class);
    when(customValueFetcherDispatcher.supports("shortname-valuefetcher")).thenReturn(true);

    var objectFieldValidator = new ObjectFieldValidator(customValueFetcherDispatcher);

    var thrown = assertThrows(InvalidConfigurationException.class, () -> objectFieldValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("ValueFetcher 'shortname-valuefetcher-invalid' is not supported for field Brewery.shortName!"));
  }

  @Test
  void validate_throwsException_withNoCustomValueFetcherDispatcher() {
    var schema = dwsReader.read("validators/dotwebstack-with-valid-value-fetcher.yaml");

    var objectFieldValidator = new ObjectFieldValidator(null);

    var thrown = assertThrows(InvalidConfigurationException.class, () -> objectFieldValidator.validate(schema));

    assertThat(thrown.getMessage(),
        is("ValueFetcher 'shortname-valuefetcher' is not supported for field Brewery.shortName!"));
  }
}
