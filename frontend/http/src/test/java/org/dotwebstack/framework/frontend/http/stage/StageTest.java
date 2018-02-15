package org.dotwebstack.framework.frontend.http.stage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StageTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Layout layout;

  @Mock
  private Site siteMock;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Test
  public void build_CreatesStage_WithValidData() {
    // Act
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).layout(layout).title(DBEERPEDIA.TITLE).build();

    // Arrange
    when(siteMock.isMatchAllDomain()).thenReturn(Boolean.FALSE);
    when(siteMock.getDomain()).thenReturn(DBEERPEDIA.NL_HOST);

    // Assert
    assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
    assertThat(stage.getTitle(), equalTo(DBEERPEDIA.TITLE));
    assertThat(stage.getFullPath(),
        equalTo("/" + DBEERPEDIA.NL_HOST + DBEERPEDIA.BASE_PATH.stringValue()));
  }

  @Test
  public void build_CreatesStage_WithValidDataAndBNode() {
    // Act
    final BNode blankNode = valueFactory.createBNode();
    Stage stage =
        new Stage.Builder(blankNode, siteMock).basePath(DBEERPEDIA.BASE_PATH.stringValue()).layout(
            layout).title(DBEERPEDIA.TITLE).build();

    // Arrange
    when(siteMock.isMatchAllDomain()).thenReturn(Boolean.FALSE);
    when(siteMock.getDomain()).thenReturn(DBEERPEDIA.NL_HOST);

    // Assert
    assertThat(stage.getIdentifier(), equalTo(blankNode));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
    assertThat(stage.getTitle(), equalTo(DBEERPEDIA.TITLE));
    assertThat(stage.getFullPath(),
        equalTo("/" + DBEERPEDIA.NL_HOST + DBEERPEDIA.BASE_PATH.stringValue()));
  }

  @Test
  public void build_CreatesStage_WhenMatchAllDomain() {
    // Act
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).layout(layout).build();

    // Arrange
    when(siteMock.isMatchAllDomain()).thenReturn(Boolean.TRUE);

    // Assert
    assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
    assertThat(stage.getFullPath(),
        equalTo("/" + Stage.PATH_DOMAIN_PARAMETER + DBEERPEDIA.BASE_PATH.stringValue()));
  }

  @Test
  public void build_CreatesStageDefaults_WhenBasePathNotProvided() {
    // Act
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).layout(layout).build();

    // Arrange
    when(siteMock.isMatchAllDomain()).thenReturn(Boolean.FALSE);
    when(siteMock.getDomain()).thenReturn(DBEERPEDIA.NL_HOST);

    // Assert
    assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(Stage.DEFAULT_BASE_PATH));
    assertThat(stage.getFullPath(), equalTo("/" + DBEERPEDIA.NL_HOST + Stage.DEFAULT_BASE_PATH));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Stage.Builder(null, siteMock).build();
  }

  @Test
  public void build_ThrowsException_WithMissingSite() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Stage.Builder(DBEERPEDIA.BREWERIES, null).build();
  }

  @Test
  public void basePath_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).basePath(null).build();
  }

  @Test
  public void layout_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).layout(null).build();
  }

}
