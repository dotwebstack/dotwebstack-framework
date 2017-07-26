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
  InformationProduct informationProduct;

  private Registry registry;

  @Before
  public void setUp() {
    registry = new Registry();
  }

  @Test
  public void testRegisterInformationProduct() {
    // Arrange
    when(informationProduct.getIdentifier()).thenReturn(identifier);
    registry.registerInformationProduct(informationProduct);

    // Act
    InformationProduct registeredInformationProduct = registry.getInformationProduct(identifier);

    // Assert
    assertEquals(informationProduct, registeredInformationProduct);
    assertEquals(1, registry.getNumberOfInformationProducts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetProductNotFound() {
    // Act & Assert
    registry.getInformationProduct(identifier);
  }

}
