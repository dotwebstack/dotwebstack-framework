package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.dotwebstack.framework.ApplicationProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class HtmlTemplateLoaderTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private Object templateSourceMock;

  @Mock
  private Resource templateResource;

  @Mock
  private InputStream inputStream;

  private ResourceLoader resourceLoader;

  private HtmlTemplateLoader loader;

  @Before
  public void setUp() throws Exception {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    when(applicationProperties.getResourcePath()).thenReturn("file:config");

    loader = new HtmlTemplateLoader(applicationProperties, resourceLoader);
  }

  @Test
  public void getLastModified_IsAlwaysMinusOne() {
    // Arrange

    // Act
    long result = loader.getLastModified(templateSourceMock);

    // Assert
    assertThat(result, is((long)-1));
  }

  @Test
  public void findTemplateSource_ReturnsNull_ForUnknownTemplate() {
    // Arrange
    when(resourceLoader.getResource("file:config/errorpages/some-unknown-template.html"))
        .thenReturn(templateResource);
    when(templateResource.exists()).thenReturn(false);

    // Act
    Object result = loader.findTemplateSource("some-unknown-template");

    // Assert
    assertNull(result);
  }

  @Test
  public void findTemplateSource_ReturnsTemplate_ForKnownTemplate() {
    // Arrange
    when(resourceLoader.getResource("file:config/errorpages/some-existing-template.html"))
        .thenReturn(templateResource);
    when(templateResource.exists()).thenReturn(true);

    // Act
    Object result = loader.findTemplateSource("some-existing-template");

    // Assert
    assertThat(result,is(templateResource));
  }

  @Test
  public void getReader_ReturnsReader() throws IOException {
    // Arrange
    when(templateResource.getInputStream()).thenReturn(inputStream);

    // Act
    Reader result = loader.getReader(templateResource,"UTF-8");

    // Assert
    assertNotNull(result);
  }

}
