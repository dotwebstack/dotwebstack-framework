package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.frontend.ld.redirection.Redirection;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RedirectionRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Redirection redirection;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private UriInfo uriInfo;

  @Mock
  URI uri;

  private RedirectionRequestHandler redirectionRequestHandler;

  @Before
  public void setUp() {
    redirectionRequestHandler = new RedirectionRequestHandler(redirection);

    // Arrange
    when(redirection.getUrlPattern()).thenReturn(DBEERPEDIA.ID2DOC_URL_PATTERN.stringValue());
    when(redirection.getTargetUrl()).thenReturn(DBEERPEDIA.ID2DOC_TARGET_URL.stringValue());

    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getAbsolutePath()).thenReturn(uri);
    when(uri.getHost()).thenReturn(DBEERPEDIA.NL_HOST);
  }

  @Test
  public void apply_ReturnValidRedirection() throws URISyntaxException {
    // Arrange
    when(uri.getPath()).thenReturn("/" + DBEERPEDIA.NL_HOST + DBEERPEDIA.BREWERY_ID_PATH);

    // Act
    Response response = redirectionRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.SEE_OTHER.getStatusCode()));
    assertThat(response.getLocation(), equalTo(new URI(DBEERPEDIA.BREWERY_DOC_PATH)));
  }

  @Test
  public void apply_ReturnUnchangedRedirection() throws URISyntaxException {
    // Arrange
    when(uri.getPath()).thenReturn("/" + DBEERPEDIA.NL_HOST + DBEERPEDIA.URL_PATTERN_VALUE);

    // Act
    Response response = redirectionRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.SEE_OTHER.getStatusCode()));
    assertThat(response.getLocation(), equalTo(new URI(DBEERPEDIA.URL_PATTERN_VALUE)));
  }

  @Test
  public void apply_ReturnServerError_WithInvalidPath() {
    // Arrange
    when(uri.getPath()).thenReturn("/" + DBEERPEDIA.NL_HOST + "some invalid pattern");

    // Act
    Response response = redirectionRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo((Status.INTERNAL_SERVER_ERROR.getStatusCode())));
  }

}
