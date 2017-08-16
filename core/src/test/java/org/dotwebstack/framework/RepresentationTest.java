package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationTest {

  @Test
  public void builder() {
    // [todo] add stage to representation object
    // Act
    final InformationProduct informationProduct =
        new InformationProduct.Builder(DBEERPEDIA.BREWERIES)
            .label(DBEERPEDIA.BREWERIES_LABEL.stringValue())
            .build();

    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES)
            .inforamationProduct(informationProduct)
            .urlPattern(DBEERPEDIA.URL_PATTERN)
            .build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getInformationProduct().getLabel(),
        equalTo(informationProduct.getLabel()));
    assertThat(representation.getUrlPattern(), equalTo(DBEERPEDIA.URL_PATTERN));
  }

}
