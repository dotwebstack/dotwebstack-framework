package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegistryTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private IRI identifier;

  @Mock
  private Backend backend;

  @Mock
  private InformationProduct informationProduct;

  private Registry registry;

  @Before
  public void setUp() {
    registry = new Registry();
  }

  @Test
  public void registerBackend() {
    // Arrange
    when(backend.getIdentifier()).thenReturn(identifier);

    // Act
    registry.registerBackend(backend);

    // Assert
    Backend backend = registry.getBackend(identifier);
    assertThat(backend, equalTo(backend));
    assertThat(registry.getNumberOfBackends(), equalTo(1));
  }

  @Test
  public void registerInformationProduct() {
    // Arrange
    when(informationProduct.getIdentifier()).thenReturn(identifier);

    // Act
    registry.registerInformationProduct(informationProduct);

    // Assert
    InformationProduct registeredInformationProduct = registry.getInformationProduct(identifier);
    assertThat(registeredInformationProduct, Matchers.equalTo(informationProduct));
    assertThat(registry.getNumberOfInformationProducts(), Matchers.equalTo(1));
  }

  @Test
  public void backendNotFound() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(String.format("Backend <%s> not found.", identifier));

    // Act
    registry.getBackend(identifier);
  }

  @Test
  public void informationProductNotFound() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(String.format("Information product <%s> not found."));

    // Act
    registry.getInformationProduct(identifier);
  }

}
