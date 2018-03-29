package org.dotwebstack.framework.frontend.ld.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service.Builder;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private Representation subRepresentation;

  @Mock
  private Site site;

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    Resource identifier = null;
    new Service.Builder(identifier).build();
  }

  @Test
  public void build_ThrowsException_WithMissingRepresentation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    Representation representation = null;
    new Service.Builder(representation).build();
  }

  @Test
  public void build_CreateService_WithValidData() {
    // Assert
    Service service = new Builder(DBEERPEDIA.SERVICE_POST).build();

    // Act
    assertThat(service.getIdentifier(), equalTo(DBEERPEDIA.SERVICE_POST));
  }

  @Test
  public void build_CreateServiceComplete_WithValidData() {
    // Assert
    Service service = new Builder(DBEERPEDIA.SERVICE_POST).build();

    // Act
    assertThat(service.getIdentifier(), equalTo(DBEERPEDIA.SERVICE_POST));
  }

  @Test
  public void build_CreateService_WithCompleteData() {
    // Act
    final Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    final Appearance appearance = new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE,
        ELMO.RESOURCE_APPEARANCE, new LinkedHashModel()).build();
    final Service service =
        (Service) new Builder(DBEERPEDIA.SERVICE_POST).informationProduct(informationProduct).stage(
            stage).appearance(appearance).appliesTo(
                DBEERPEDIA.PATH_PATTERN_VALUE).subRepresentation(subRepresentation).build();

    // Assert
    assertThat(service.getIdentifier(), equalTo(DBEERPEDIA.SERVICE_POST));
    assertThat(service.getInformationProduct(), equalTo(informationProduct));
    assertThat(service.getStage(), equalTo(stage));
    assertThat(service.getAppearance(), equalTo(appearance));
    assertThat(service.getAppliesTo().toArray()[0], equalTo(DBEERPEDIA.PATH_PATTERN_VALUE));
    assertThat(service.getAppliesTo().size(), equalTo(1));
    assertThat(service.getSubRepresentations(), equalTo(ImmutableList.of(subRepresentation)));
  }

}
