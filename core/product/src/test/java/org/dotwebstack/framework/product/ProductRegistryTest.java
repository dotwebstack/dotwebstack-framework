package org.dotwebstack.framework.product;

import static org.junit.Assert.assertEquals;

import org.dotwebstack.framework.product.config.ProductRegistry;
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
    String productName = "movies";
    Product product = new Product(new Source() {});
    productRegistry.registerProduct("movies", product);

    assertEquals(product, productRegistry.getProduct(productName));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetProductNotFound() {
    productRegistry.getProduct("actors");
  }

}
