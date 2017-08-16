package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductTest {

  @Mock
  BackendSource backendSource;

  @Test
  public void builder() {
    // Act
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES, backendSource).label(
            DBEERPEDIA.BREWERIES_LABEL.stringValue()).build();

    // Assert
    assertThat(informationProduct.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(informationProduct.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void builderWithNullValues() {
    // Act
    InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES, backendSource).label(null).build();

    assertThat(informationProduct.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(informationProduct.getLabel(), equalTo(null));
  }

}
