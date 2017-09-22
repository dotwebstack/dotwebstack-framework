package org.dotwebstack.framework.frontend.http.stage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.test.DBEERPEDIA;
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
  private Site siteMock;

  @Test
  public void build_CreatesStage_WithValidData() {
    // Act
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();

    // Assert
    assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
  }

  @Test
  public void build_CreatesStageDefaults_WhenNotProvided() {
    // Act
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).build();

    // Assert
    assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(stage.getSite(), equalTo(siteMock));
    assertThat(stage.getBasePath(), equalTo(Stage.DEFAULT_BASE_PATH));
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

}
