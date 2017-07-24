package org.dotwebstack.framework;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.model.IRI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductRegistryTest {

  @Mock
  IRI identifier;

  @Mock
  Product product;

  private ProductRegistry productRegistry;

  @Before
  public void setUp() {
    productRegistry = new ProductRegistry();
  }

  @Test
  public void testRegisterProduct() {
    // Arrange
    when(product.getIdentifier()).thenReturn(identifier);
    productRegistry.registerProduct(product);

    // Act
    Product registeredProduct = productRegistry.getProduct(identifier);

    // Assert
    assertEquals(product, registeredProduct);
    assertEquals(1, productRegistry.getNumberOfProducts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetProductNotFound() {
    // Act & Assert
    productRegistry.getProduct(identifier);
  }

}
