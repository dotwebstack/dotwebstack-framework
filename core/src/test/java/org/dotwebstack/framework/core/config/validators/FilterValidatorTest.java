package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.FieldConfigurationImpl;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.junit.jupiter.api.Test;

class FilterValidatorTest {

  private final DotWebStackConfigurationReader dwsReader =
      new DotWebStackConfigurationReader(TypeConfigurationImpl.class, FieldConfigurationImpl.class);

  @Test
  void validate_throwsNoException_withValidFilterFields() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-valid-filter.yaml");

    new FilterValidator().validate(dotWebStackConfiguration);
  }

  @Test
  void validate_throwsNoException_withInvalidFilterFields() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-invalid-filter.yaml");

    FilterValidator validator = new FilterValidator();
    var exception =
        assertThrows(InvalidConfigurationException.class, () -> validator.validate(dotWebStackConfiguration));

    assertThat(exception.getMessage(),
        is("Filter field 'brewery.visitAddress.invalid' in object type 'Brewery' can't be resolved."));
  }
}
