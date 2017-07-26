package org.dotwebstack.framework.config;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.dotwebstack.framework.InformationProduct;
import org.dotwebstack.framework.Registry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class ConfigLoaderTest {

  @Mock
  ConfigProperties configProperties;

  @Mock
  Registry registry;

  @Mock
  ResourcePatternResolver resourceLoader;

  private ConfigLoader configLoader;

  @Before
  public void setUp() {
    configLoader = new ConfigLoader(configProperties, registry);
    configLoader.setResourceLoader(resourceLoader);
  }

  @Test
  public void testLoadEmptyConfiguration() throws IOException {
    // Arrange
    when(resourceLoader.getResources(any(String.class))).thenReturn(new Resource[0]);

    // Act
    configLoader.loadConfiguration();

    // Assert
    assertEquals(0, registry.getNumberOfInformationProducts());
  }

  @Test
  public void testLoadSingleConfigurationFile() throws IOException {
    // Arrange
    Resource moviesResource = new ClassPathResource("config/moviedb.ttl");
    when(resourceLoader.getResources(any())).thenReturn(new Resource[] {moviesResource});

    // Act
    configLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<InformationProduct> captureProducts =
        ArgumentCaptor.forClass(InformationProduct.class);
    verify(registry, times(2)).registerInformationProduct(captureProducts.capture());
    List<String> identifiers =
        captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(
            toList());

    assertThat("Should contain actors and movies", identifiers,
        hasItems("http://moviedb.org#Actors", "http://moviedb.org#Movies"));
  }

  @Test
  public void testLoadMultipleConfigurationFiles() throws IOException {
    // Arrange
    Resource actorsResource = new ClassPathResource("config/cinemadb.ttl");
    Resource moviesResource = new ClassPathResource("config/moviedb.ttl");
    Resource nonrdfResource = new ClassPathResource("config/nonrdf.md");
    when(resourceLoader.getResources(any())).thenReturn(
        new Resource[] {actorsResource, moviesResource, nonrdfResource});

    // Act
    configLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<InformationProduct> captureProducts =
        ArgumentCaptor.forClass(InformationProduct.class);
    verify(registry, times(3)).registerInformationProduct(captureProducts.capture());
    List<String> identifiers =
        captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(
            toList());

    assertThat("Should contain both movies and actors", identifiers, hasItems(
        "http://cinemadb.org#Cinemas", "http://moviedb.org#Actors", "http://moviedb.org#Movies"));
  }

  @Test(expected = IOException.class)
  public void testLoadNonExistingPath() throws IOException {
    // Arrange
    when(resourceLoader.getResources(any(String.class))).thenThrow(IOException.class);

    // Act
    configLoader.loadConfiguration();

    // Assert
    verify(registry, never()).registerInformationProduct(any());
  }

  @Test
  public void testLoadXmlConfigurationFile() throws IOException {
    // Arrange
    Resource moviesResource = new ClassPathResource("config/moviedb.xml");
    when(resourceLoader.getResources(any(String.class))).thenReturn(
        new Resource[] {moviesResource});

    // Act
    configLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<InformationProduct> captureProducts =
        ArgumentCaptor.forClass(InformationProduct.class);
    verify(registry, times(2)).registerInformationProduct(captureProducts.capture());
    List<String> identifiers =
        captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(
            toList());

    assertThat("Should contain both movies and actors", identifiers,
        hasItems("http://moviedb.org#Actors", "http://moviedb.org#Movies"));
  }


  @Test(expected = ConfigException.class)
  public void testLoadInvalidFormat() throws IOException {
    // Arrange
    Resource invalidResource = new ClassPathResource("config/invalid.ttl");
    when(resourceLoader.getResources(any(String.class))).thenReturn(
        new Resource[] {invalidResource});

    // Act
    configLoader.loadConfiguration();

    // Assert
    verify(registry, never()).registerInformationProduct(any());
  }

}
