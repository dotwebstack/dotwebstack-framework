package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServletErrorHandlerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext context;

  @Mock
  private UriInfo uriInfo;

  private ServletErrorHandler servletErrorHandler;

  private MultivaluedHashMap<String, String> pathParameters;

  @Before
  public void setUp() {
    servletErrorHandler = new ServletErrorHandler();
    pathParameters = new MultivaluedHashMap<>();
    when(context.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPathParameters()).thenReturn(pathParameters);
  }

  @Test
  public void apply_ReturnsResponseBasedOnStatusCodeParameter_WhenStatusCodeIsKnown() {
    // Arrange
    pathParameters.add("statusCode", "406");

    // Act
    Response response = servletErrorHandler.apply(context);

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.NOT_ACCEPTABLE));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.getEntity().toString(), equalTo(Status.NOT_ACCEPTABLE.getReasonPhrase()));
  }

  @Test
  public void apply_ThrowsException_WhenStatusCodeIsMissing() {
    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Path parameter 'statusCode' is required.");

    // Act
    servletErrorHandler.apply(context);
  }

  @Test
  public void apply_ThrowsException_WhenStatusCodeIsUnknown() {
    // Arrange
    pathParameters.add("statusCode", "555");

    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Status code '555' is unknown.");

    // Act
    servletErrorHandler.apply(context);
  }

}
