package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductFactoryTest {

  @Mock
  private IRI identifier;

  private InformationProductFactory informationProductFactory;

  @Before
  public void setUp() {
    informationProductFactory = new InformationProductFactory();
  }

  @Test
  public void informationProductWithAllProperties() {
    // Arrange
    Model configurationModel =
        new ModelBuilder().add(identifier, RDFS.LABEL, DBEERPEDIA.BACKEND_LABEL).build();

    // Act
    InformationProduct informationProduct =
        informationProductFactory.create(configurationModel, identifier);

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(identifier));
    assertThat(informationProduct.getLabel(), equalTo(DBEERPEDIA.BACKEND_LABEL.stringValue()));
  }

  @Test
  public void informationProductWithoutNonRequiredProperties() {
    // Arrange
    Model configurationModel = new ModelBuilder().build();

    // Act
    InformationProduct informationProduct =
        informationProductFactory.create(configurationModel, identifier);

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(identifier));
    assertThat(informationProduct.getLabel(), equalTo(null));
  }

}
