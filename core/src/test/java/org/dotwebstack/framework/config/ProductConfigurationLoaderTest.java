package org.dotwebstack.framework.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.dotwebstack.framework.ProductRegistry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

public class ProductConfigurationLoaderTest {

  ProductConfigurationLoader productConfigurationLoader;

  ProductProperties productProperties;

  ProductRegistry productRegistry;

  IRI movies, actors;

  @Before
  public void setUp() {
    productProperties = new ProductProperties();
    productRegistry = new ProductRegistry();
    productConfigurationLoader = new ProductConfigurationLoader(productProperties, productRegistry);
    productConfigurationLoader.setResourceLoader(null);
    movies = SimpleValueFactory.getInstance().createIRI("http://moviedb.org/product#Movies");
    actors = SimpleValueFactory.getInstance().createIRI("http://moviedb.org/product#Actors");
  }

  @Test
  public void testLoadEmptyConfiguration() throws IOException {
    productProperties.setConfigPath("empty");
    productConfigurationLoader.loadConfiguration();

    assertEquals(0, productRegistry.getNumberOfProducts());
  }

  @Test
  public void testLoadSingleConfigurationFile() throws IOException {
    productProperties.setConfigPath("single");
    productConfigurationLoader.loadConfiguration();

    assertEquals(2, productRegistry.getNumberOfProducts());
    assertEquals(movies, productRegistry.getProduct(movies).getIdentifier());
    assertEquals(actors, productRegistry.getProduct(actors).getIdentifier());
  }

  @Test
  public void testLoadMultipleConfigurationFiles() throws IOException {
    productProperties.setConfigPath("multiple");
    productConfigurationLoader.loadConfiguration();

    assertEquals(2, productRegistry.getNumberOfProducts());
    assertEquals(movies, productRegistry.getProduct(movies).getIdentifier());
    assertEquals(actors, productRegistry.getProduct(actors).getIdentifier());
  }

  @Test(expected = IOException.class)
  public void testLoadNonExistingPath() throws IOException {
    productProperties.setConfigPath("non-existing");
    productConfigurationLoader.loadConfiguration();
  }

  @Test(expected = ProductConfigurationException.class)
  public void testLoadInvalidFormat() throws IOException {
    productProperties.setConfigPath("invalid");
    productConfigurationLoader.loadConfiguration();
  }

}
