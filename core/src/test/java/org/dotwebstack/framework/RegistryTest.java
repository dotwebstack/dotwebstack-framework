package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.site.Site;
import org.dotwebstack.framework.stage.Stage;
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
  private Site site;

  @Mock
  private Stage stage;

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
    assertThat(registry.getNumberOfBackends(), equalTo(1));
  }

  @Test
  public void registerSite() {
    // Arrange
    when(site.getIdentifier()).thenReturn(identifier);

    // Act
    registry.registerSite(site);

    // Assert
    Site registeredSite = registry.getSite(identifier);
    assertThat(registeredSite, Matchers.equalTo(site));
    assertThat(registry.getNumberOfSites(), Matchers.equalTo(1));
  }

  @Test
  public void registerStage() {
    // Arrange
    when(stage.getIdentifier()).thenReturn(identifier);

    // Act
    registry.registerStage(stage);

    // Assert
    Stage registeredStage = registry.getStage(identifier);
    assertThat(registeredStage, Matchers.equalTo(stage));
    assertThat(registry.getNumberOfStages(), Matchers.equalTo(1));
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
