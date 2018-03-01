package org.dotwebstack.framework.frontend.ld.endpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EndPointTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final String pathPattern = "pathPattern";

  @Mock
  private Stage stage;

  @Test
  public void build_CreateEndPoint_WithValidData() {
    // Assert
    EndPoint endPoint = new EndPoint.Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).build();

    // Act
    assertThat(endPoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(endPoint.getPathPattern(), equalTo(pathPattern));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new EndPoint.Builder(null, pathPattern).build();
  }

  @Test
  public void build_ThrowsException_WithMissingPathPattern() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new EndPoint.Builder(DBEERPEDIA.DOC_ENDPOINT, null).build();
  }

  @Test
  public void build_CreateEndpointComplete_WithValidData() {
    // Assert
    EndPoint endPoint =
        new EndPoint.Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).label("label").stage(
            stage).build();

    // Act
    assertThat(endPoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(endPoint.getPathPattern(), equalTo(pathPattern));
    assertThat(endPoint.getStage(), equalTo(stage));
    assertThat(endPoint.getLabel(), equalTo("label"));
  }

}
