package org.dotwebstack.framework.frontend.ld.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  BackendSource backendSource;

  @Test
  public void builder() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES,
            DBEERPEDIA.URL_PATTERN.stringValue())
            .build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(null));
    assertThat(representation.getStage(), equalTo(null));
    assertThat(representation.getUrlPattern(), equalTo(DBEERPEDIA.URL_PATTERN.stringValue()));
  }

  @Test
  public void builderWithNullValues() {
    // [todo] add stage to representation object
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES, null).build();
  }

}
