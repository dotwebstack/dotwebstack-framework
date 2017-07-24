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
import org.dotwebstack.framework.Product;
import org.dotwebstack.framework.ProductRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigurationLoaderTest {

  @Mock
  ProductProperties productProperties;

  @Mock
  ProductRegistry productRegistry;

  private ProductConfigurationLoader productConfigurationLoader;

  @Before
  public void setUp() {
    productConfigurationLoader = new ProductConfigurationLoader(productProperties, productRegistry);
  }

  @Test
  public void testLoadEmptyConfiguration() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("empty");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    assertEquals(0, productRegistry.getNumberOfProducts());
  }

  @Test
  public void testLoadSingleConfigurationFile() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("single");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<Product> captureProducts = ArgumentCaptor.forClass(Product.class);
    verify(productRegistry, times(2)).registerProduct(captureProducts.capture());
    List<String> identifiers = captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(toList());

    assertThat("Should contain both movies and actors", identifiers,
        hasItems("http://moviedb.org/product#Actors", "http://moviedb.org/product#Movies"));
  }

  @Test
  public void testLoadMultipleConfigurationFiles() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("multiple");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<Product> captureProducts = ArgumentCaptor.forClass(Product.class);
    verify(productRegistry, times(2)).registerProduct(captureProducts.capture());
    List<String> identifiers = captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(toList());

    assertThat("Should contain both movies and actors", identifiers,
        hasItems("http://moviedb.org/product#Actors", "http://moviedb.org/product#Movies"));
  }

  @Test(expected = IOException.class)
  public void testLoadNonExistingPath() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("non-existing");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    verify(productRegistry, never()).registerProduct(any());
  }

  @Test
  public void testLoadXmlConfigurationFile() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("rdf-xml");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    ArgumentCaptor<Product> captureProducts = ArgumentCaptor.forClass(Product.class);
    verify(productRegistry, times(2)).registerProduct(captureProducts.capture());
    List<String> identifiers = captureProducts.getAllValues().stream().map(p -> p.getIdentifier().toString()).collect(toList());

    assertThat("Should contain both movies and actors", identifiers,
        hasItems("http://moviedb.org/product#Actors", "http://moviedb.org/product#Movies"));
  }


  @Test(expected = ProductConfigurationException.class)
  public void testLoadInvalidFormat() throws IOException {
    // Arrange
    when(productProperties.getConfigPath()).thenReturn("invalid");

    // Act
    productConfigurationLoader.loadConfiguration();

    // Assert
    verify(productRegistry, never()).registerProduct(any());
  }

}
