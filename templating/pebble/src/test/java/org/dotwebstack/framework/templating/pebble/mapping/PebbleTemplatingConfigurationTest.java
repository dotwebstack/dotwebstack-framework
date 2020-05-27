package org.dotwebstack.framework.templating.pebble.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.core.CoreProperties;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.junit.jupiter.api.Test;

class PebbleTemplatingConfigurationTest {

  private PebbleTemplatingConfiguration templatingConfiguration;

  @Test
  void testConfiguration_readsTemplates_successfully() {
    // Arrange
    templatingConfiguration =
        new PebbleTemplatingConfiguration(new ResourceLoaderUtils(new CoreProperties()), Collections.emptyList());

    // Act
    Map<String, PebbleTemplate> templateMap = templatingConfiguration.htmlTemplates();

    // Assert
    assertThat(templateMap.containsKey("base.html"), is(true));
    assertThat(templateMap.containsKey("correct.html"), is(true));
  }

}
