package org.dotwebstack.framework.product;

import org.dotwebstack.framework.product.config.ProductConfigLoader;
import org.dotwebstack.framework.product.config.ProductProperties;
import org.dotwebstack.framework.product.config.ProductRegistry;
import org.junit.Before;
import org.junit.Test;

public class ProductConfigLoaderTest {

  ProductConfigLoader productConfigLoader;

  @Before
  public void setUp() {
    productConfigLoader = new ProductConfigLoader(new ProductProperties(), new ProductRegistry());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testLoad() {
    productConfigLoader.load();
  }

}
