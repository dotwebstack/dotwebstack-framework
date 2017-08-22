package org.dotwebstack.framework.frontend.ld.representation;

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
public class RepresentationTest {

  @Mock
  BackendSource backendSource;

  @Test
  public void builder() {
    // [todo] add stage to representation object
    // Act
    final InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES, backendSource)
            .label(DBEERPEDIA.BREWERIES_LABEL.stringValue())
            .build();

    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES, informationProduct,
            DBEERPEDIA.URL_PATTERN.stringValue()).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getInformationProduct().getLabel(),
        equalTo(informationProduct.getLabel()));
    assertThat(representation.getUrlPattern(), equalTo(DBEERPEDIA.URL_PATTERN));
  }

  @Test
  public void builderWithNullValues() {
    // [todo] add stage to representation object
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES, null, null).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(null));
    assertThat(representation.getUrlPattern(), equalTo(null));
  }

}
