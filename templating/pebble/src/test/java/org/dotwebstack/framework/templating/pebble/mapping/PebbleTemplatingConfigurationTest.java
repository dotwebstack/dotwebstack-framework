package org.dotwebstack.framework.templating.pebble.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class PebbleTemplatingConfigurationTest {

  @Mock
  private ResourcePatternResolver resourceLoader;

  private PebbleTemplatingConfiguration templatingConfiguration;

  @Test
  void testConfiguration_readsTemplates_successfully() throws IOException {
    // Arrange
    Resource baseHtmlResource = mock(Resource.class);
    when(baseHtmlResource.getFilename()).thenReturn("base.html");
    when(baseHtmlResource.exists()).thenReturn(true);

    Resource correctHtmlResource = mock(Resource.class);
    when(correctHtmlResource.getFilename()).thenReturn("correct.html");
    when(correctHtmlResource.exists()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {baseHtmlResource, correctHtmlResource});

    // Arrange
    templatingConfiguration = new PebbleTemplatingConfiguration(resourceLoader, Collections.emptyList());

    // Act
    Map<String, PebbleTemplate> templateMap = templatingConfiguration.htmlTemplates();

    // Assert
    assertThat(templateMap.containsKey("base.html"), is(true));
    assertThat(templateMap.containsKey("correct.html"), is(true));
  }

}
