package org.dotwebstack.framework.frontend.ld.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
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

  @Mock
  private Representation subRepresentation;

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    IRI representationIri = null;
    new Representation.Builder(representationIri).build();
  }

  @Test
  public void build_ThrowsException_WithMissingRepreseentation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    Representation representation = null;
    new Representation.Builder(representation).build();
  }

  @Test
  public void build_CreatesRepresentation_WithValidData() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPattern("urlPattern").build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), is(nullValue()));
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getAppearance(), is(nullValue()));
    assertThat(representation.getUrlPatterns(), hasItem("urlPattern"));
    assertThat(representation.getSubRepresentations(), is(Collections.EMPTY_LIST));
  }

  @Test
  public void build_CreatesRepresentation_WithoutUrlPattern() {
    // Act
    final Representation representation = new Representation.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), is(nullValue()));
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getUrlPatterns(), equalTo(Collections.EMPTY_LIST));
    assertThat(representation.getAppearance(), is(nullValue()));
  }

  @Test
  public void build_CreatesRepresentation_WithUrlPattern() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPattern(
            DBEERPEDIA.URL_PATTERN_VALUE).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
  }

  @Test
  public void build_CreatesRepresentation_WithMultipleUrlPattern() {
    // Act
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).urlPattern(
            DBEERPEDIA.URL_PATTERN_VALUE).urlPattern(DBEERPEDIA.URL_PATTERN_VALUE).urlPattern(
                DBEERPEDIA.URL_PATTERN_VALUE).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), is(nullValue()));
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(representation.getUrlPatterns().size(), equalTo(3));
  }

  @Test
  public void build_CreatesRepresentation_WithCompleteData() {
    // Act
    final Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    final Appearance appearance = new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE,
        ELMO.RESOURCE_APPEARANCE, new LinkedHashModel()).build();
    final Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
            informationProduct).stage(stage).appearance(appearance).urlPattern(
                DBEERPEDIA.URL_PATTERN_VALUE).subRepresentation(subRepresentation).build();

    // Assert
    assertThat(representation.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getStage(), equalTo(stage));
    assertThat(representation.getAppearance(), equalTo(appearance));
    assertThat(representation.getUrlPatterns().toArray()[0], equalTo(DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(representation.getUrlPatterns().size(), equalTo(1));
    assertThat(representation.getSubRepresentations(),
        equalTo(ImmutableList.of(subRepresentation)));
  }

}
