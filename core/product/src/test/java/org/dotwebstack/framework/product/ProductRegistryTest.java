package org.dotwebstack.framework.product;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

public class ProductRegistryTest {

  ProductRegistry productRegistry;

  @Before
  public void setUp() {
    productRegistry = new ProductRegistry();
  }

  @Test
  public void testRegisterProduct() {
    IRI identifier =
        SimpleValueFactory.getInstance().createIRI("http://moviedb.org/product#Movies");
    Source source = new Source() {};
    Product product = new Product(identifier, source);
    productRegistry.registerProduct(product);
    Product registeredProduct = productRegistry.getProduct(identifier);

    assertEquals(product, registeredProduct);
    assertEquals(identifier, registeredProduct.getIdentifier());
    assertEquals(source, registeredProduct.getSource());
    assertEquals(1, productRegistry.getNumberOfProducts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetProductNotFound() {
    IRI identifier =
        SimpleValueFactory.getInstance().createIRI("http://moviedb.org/product#NotExisting");
    productRegistry.getProduct(identifier);
  }

}
