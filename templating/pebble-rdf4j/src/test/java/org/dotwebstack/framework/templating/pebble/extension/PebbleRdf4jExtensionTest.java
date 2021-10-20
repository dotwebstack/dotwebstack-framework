package org.dotwebstack.framework.templating.pebble.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mitchellbosecke.pebble.extension.Filter;
import java.util.Map;
import org.dotwebstack.framework.templating.pebble.filter.JsonLdFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PebbleRdf4jExtensionTest {

  @Mock
  private JsonLdFilter jsonLdFilterMock;

  @Test
  void pebbleExtensionShouldHaveOneFilter() {
    // Arrange
    PebbleRdf4jExtension pebbleRdf4jExtension = new PebbleRdf4jExtension(jsonLdFilterMock);

    // Act
    Map<String, Filter> filters = pebbleRdf4jExtension.getFilters();

    // Assert
    assertThat(filters.size(), is(1));
    assertThat(filters.containsKey("jsonld"), is(true));
  }
}
