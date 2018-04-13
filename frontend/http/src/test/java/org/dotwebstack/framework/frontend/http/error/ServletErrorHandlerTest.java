package org.dotwebstack.framework.frontend.http.error;

import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
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
  public void apply_ReturnsExceptionBasedOnStatusCodeParameter_WhenStatusCodeIsKnown() {
    // Arrange
    pathParameters.add("statusCode", "406");

    // Assert
    thrown.expect(WebApplicationException.class);
    thrown.expectMessage("HTTP 406 Not Acceptable");

    // Act
    servletErrorHandler.apply(context);

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
