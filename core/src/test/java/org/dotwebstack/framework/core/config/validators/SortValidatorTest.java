package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.FieldConfigurationImpl;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.junit.jupiter.api.Test;

class SortValidatorTest {

  private final DotWebStackConfigurationReader dwsReader =
      new DotWebStackConfigurationReader(TypeConfigurationImpl.class, FieldConfigurationImpl.class);

  @Test
  void validate_throwsNoException_withValidSortableByFields() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-valid-sortable-by.yaml");
    initObjectTypes(dotWebStackConfiguration);
    new SortValidator().validate(dotWebStackConfiguration);
  }

  @Test
  void validate_throwsNoException_withInvalidSortableByFields() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-invalid-sortable-by.yaml");
    initObjectTypes(dotWebStackConfiguration);
    SortValidator validator = new SortValidator();
    var exception =
        assertThrows(InvalidConfigurationException.class, () -> validator.validate(dotWebStackConfiguration));

    assertThat(exception.getMessage(), is(
        "Sort field 'visitAddress.invalid' in object type 'Brewery' " + "can't be resolved to a single scalar type."));
  }

  private void initObjectTypes(DotWebStackConfiguration dotWebStackConfiguration) {
    if (dotWebStackConfiguration.getObjectTypes() != null) {
      dotWebStackConfiguration.getObjectTypes()
          .values()
          .forEach(objectType -> objectType.init(dotWebStackConfiguration));
    }
  }
}
