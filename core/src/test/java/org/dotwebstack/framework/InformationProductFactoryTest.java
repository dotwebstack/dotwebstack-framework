package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
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
  public void informationProductIsCreated() {
    // Arrange
    Model configurationModel = new ModelBuilder().build();

    // Act
    InformationProduct informationProduct =
        informationProductFactory.create(configurationModel, identifier);

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(identifier));
  }

}
