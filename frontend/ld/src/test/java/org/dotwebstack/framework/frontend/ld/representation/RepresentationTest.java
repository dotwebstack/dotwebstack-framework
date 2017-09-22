package org.dotwebstack.framework.frontend.ld.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
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
  private Site site;

  @Mock
  private InformationProduct informationProduct;

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Representation.Builder(null).build();
  }

  @Test
  public void build_CreatesRepresentation_WithValidData() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPatterns("urlPattern").build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(null));
    assertThat(representation.getStage(), equalTo(null));
    assertThat(representation.getUrlPatterns(), hasItem("urlPattern"));
  }

  @Test
  public void build_CreatesRepresentation_WithoutUrlPattern() {
    // Act
    final Representation representation = new Representation.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(null));
    assertThat(representation.getStage(), equalTo(null));
    assertThat(representation.getUrlPatterns(), equalTo(null));
  }

  @Test
  public void build_CreatesRepresentation_WithUrlPattern() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPatterns(
            DBEERPEDIA.URL_PATTERN_VALUE).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));

    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
  }

  @Test
  public void build_CreatesRepresentation_WithMultipleUrlPattern() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE,
            DBEERPEDIA.URL_PATTERN_VALUE, DBEERPEDIA.URL_PATTERN_VALUE).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(null));
    assertThat(representation.getStage(), equalTo(null));
    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(representation.getUrlPatterns().size(), equalTo(3));
  }

  @Test
  public void build_CreatesRepresentation_WithCompleteData() {
    // Act
    final Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();

    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
            informationProduct).stage(stage).urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getStage(), equalTo(stage));
    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(representation.getUrlPatterns().size(), equalTo(1));
  }

}
