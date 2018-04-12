package org.dotwebstack.framework.frontend.ld.endpoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint.Builder;
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
public class DirectEndpointTest {

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
  public void build_CreateDirectEndpoint_WithValidData() {
    // Assert
    DirectEndpoint directEndpoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).build();

    // Act
    assertThat(directEndpoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(directEndpoint.getPathPattern(), equalTo(pathPattern));
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
  public void build_CreateDirectEndpointComplete_WithValidData() {
    // Assert
    DirectEndpoint directEndpoint =
        (DirectEndpoint) new Builder(DBEERPEDIA.DOC_ENDPOINT, pathPattern).postRepresentation(
            postRepresentation).getRepresentation(getRespresentation).postService(
                postService).putService(putService).deleteService(deleteService).label(label).stage(
                    stage).build();

    // Act
    assertThat(directEndpoint.getIdentifier(), equalTo(DBEERPEDIA.DOC_ENDPOINT));
    assertThat(directEndpoint.getPathPattern(), equalTo(pathPattern));
    assertThat(directEndpoint.getLabel(), equalTo(label));
    assertThat(directEndpoint.getStage(), equalTo(stage));
    assertThat(directEndpoint.getGetRepresentation(), equalTo(getRespresentation));
    assertThat(directEndpoint.getPostRepresentation(), equalTo(postRepresentation));
    assertThat(directEndpoint.getDeleteService(), equalTo(deleteService));
    assertThat(directEndpoint.getPostService(), equalTo(postService));
    assertThat(directEndpoint.getPutService(), equalTo(putService));
  }
}
