package org.dotwebstack.framework.frontend.ld;

import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormatPreMatchingRequestFilter {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext containerRequestContext;

  private FormatPreMatchingRequestFilter formatPreMatchingRequestFilter;

  @Before
  public void setUp() {
    formatPreMatchingRequestFilter = new FormatPreMatchingRequestFilter();
  }

  @Test
  public void filter_AddAcceptHeader_WhenFormatIsJson() throws Exception {
    // Arrange
    MultivaluedStringMap queryParameters = new MultivaluedStringMap();
    queryParameters.add("format", "json");
    when(containerRequestContext.getUriInfo().getQueryParameters()).thenReturn(queryParameters);

    // Act
    // formatPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    // verify(containerRequestContext).getHeaders().put(HttpHeaders.ACCEPT,
    // Arrays.asList(MediaType.APPLICATION_JSON, MediaTypes.LDJSON));
  }

}
