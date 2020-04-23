package org.dotwebstack.framework.templating.pebble.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.net.URISyntaxException;
import java.util.Map;
import org.dotwebstack.framework.core.CoreProperties;
import org.junit.jupiter.api.Test;

class PebbleTemplatingConfigurationTest {

  private PebbleTemplatingConfiguration templatingConfiguration;

  @Test
  void testConfiguration_readsTemplates_successfully() throws URISyntaxException {
    // Arrange
    templatingConfiguration = new PebbleTemplatingConfiguration(new CoreProperties());

    // Act
    Map<String, PebbleTemplate> templateMap = templatingConfiguration.htmlTemplates();

    // Assert
    assertThat(templateMap.containsKey("base.html"), is(true));
    assertThat(templateMap.containsKey("correct.html"), is(true));
  }

}
