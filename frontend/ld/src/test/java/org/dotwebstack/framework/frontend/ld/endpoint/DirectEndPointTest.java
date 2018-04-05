package org.dotwebstack.framework.frontend.ld.endpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint.Builder;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DirectEndPointTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final String pathPattern = "pathPattern";

  private final String label = "label";

  @Mock
  private Representation getRespresentation;

  @Mock
  private Representation postRepresentation;

  @Mock
  private Service postService;

  @Mock
  private Service putService;

  @Mock
  private Service deleteService;

  @Mock
  private Stage stage;

  @Test
  public void build_CreateDirectEndPoint_WithValidData() {
    // Assert
    DirectEndPoint directEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).build();

    // Act
    assertThat(directEndPoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(directEndPoint.getPathPattern(), equalTo(pathPattern));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Builder(null, pathPattern).build();
  }

  @Test
  public void build_ThrowsException_WithMissingPathPattern() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Builder(DBEERPEDIA.DOC_ENDPOINT, null).build();
  }

  @Test
  public void build_CreateDirectEndPointComplete_WithValidData() {
    // Assert
    DirectEndPoint directEndPoint =
        (DirectEndPoint) new Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).postRepresentation(
            postRepresentation).getRepresentation(getRespresentation).postService(
                postService).putService(putService).deleteService(deleteService).label(label).stage(
                    stage).build();

    // Act
    assertThat(directEndPoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(directEndPoint.getPathPattern(), equalTo(pathPattern));
    assertThat(directEndPoint.getLabel(), equalTo(label));
    assertThat(directEndPoint.getStage(), equalTo(stage));
    assertThat(directEndPoint.getGetRepresentation(), equalTo(getRespresentation));
    assertThat(directEndPoint.getPostRepresentation(), equalTo(postRepresentation));
    assertThat(directEndPoint.getDeleteService(), equalTo(deleteService));
    assertThat(directEndPoint.getPostService(), equalTo(postService));
    assertThat(directEndPoint.getPutService(), equalTo(putService));
  }
}
