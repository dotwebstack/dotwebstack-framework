package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void builder() {
    // Act
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES).label(
            DBEERPEDIA.BREWERIES_LABEL.stringValue()).build();

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(informationProduct.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void builderWithNullValues() {
    // Act
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES).label(null).build();

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(informationProduct.getLabel(), equalTo(null));
  }

  @Test
  public void cannotGetTypeOnUndecoratedInformationProduct() {
    // Arrange
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Result type cannot be determined.");

    // Act
    informationProduct.getResultType();
  }

  @Test
  public void cannotGetResultOnUndecoratedInformationProduct() {
    // Arrange
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Result cannot be determined.");

    // Act
    informationProduct.getResult();
  }

}
