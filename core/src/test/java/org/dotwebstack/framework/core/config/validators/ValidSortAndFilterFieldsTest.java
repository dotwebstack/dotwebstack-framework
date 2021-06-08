package org.dotwebstack.framework.core.config.validators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.FieldConfigurationImpl;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.junit.jupiter.api.Test;

class ValidSortAndFilterFieldsTest {

  private final DotWebStackConfigurationReader dwsReader =
      new DotWebStackConfigurationReader(TypeConfigurationImpl.class, FieldConfigurationImpl.class);

  @Test
  void get_returnsValidFields_withDotWebStackConfiguration() {
    var dotWebStackConfiguration = dwsReader.read("validators/dotwebstack-with-valid-sort-filter-fields.yaml");

    List<String> result = ValidSortAndFilterFields.get(dotWebStackConfiguration);

    assertThat(result.size(), is(13));
    assertThat(result,
        containsInAnyOrder("brewery.identifier", "brewery.geometry", "brewery.visitAddress.identifier",
            "brewery.visitAddress.street", "brewery.visitAddress.city", "brewery.postalAddress.identifier",
            "brewery.postalAddress.street", "brewery.postalAddress.city", "beer.identifier", "beer.name",
            "address.identifier", "address.street", "address.city"));
  }
}
