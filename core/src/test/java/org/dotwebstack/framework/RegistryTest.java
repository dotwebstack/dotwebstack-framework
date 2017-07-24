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
public class RegistryTest {

  @Mock
  IRI identifier;

  @Mock
  Product product;

  private Registry registry;

  @Before
  public void setUp() {
    registry = new Registry();
  }

  @Test
  public void testRegisterProduct() {
    // Arrange
    when(product.getIdentifier()).thenReturn(identifier);
    registry.registerProduct(product);

    // Act
    Product registeredProduct = registry.getProduct(identifier);

    // Assert
    assertEquals(product, registeredProduct);
    assertEquals(1, registry.getNumberOfProducts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetProductNotFound() {
    // Act & Assert
    registry.getProduct(identifier);
  }

}
