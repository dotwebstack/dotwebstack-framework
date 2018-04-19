package org.dotwebstack.framework.frontend.http;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormatPreMatchingRequestFilterTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  MultivaluedHashMap<String, String> headers;

  @Mock
  private UriInfo uriInfo;

  private FormatPreMatchingRequestFilter formatPreMatchingRequestFilter;

  @Before
  public void setUp() {
    formatPreMatchingRequestFilter = new FormatPreMatchingRequestFilter();
  }

  @Test
  public void filter_AddAcceptHeader_WhenFormatIsJson() throws Exception {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
    queryParameters.add("format", "json");
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getHeaders()).thenReturn(headers);

    // Act
    formatPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(headers).put(HttpHeaders.ACCEPT,
        Arrays.asList(MediaType.APPLICATION_JSON, MediaTypes.LDJSON));
  }

  @Test
  public void filter_AddAcceptHeader_WhenFormatIsXml() throws Exception {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
    queryParameters.add("format", "xml");
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getHeaders()).thenReturn(headers);

    // Act
    formatPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(headers).put(HttpHeaders.ACCEPT,
        Arrays.asList(MediaType.APPLICATION_XML, MediaTypes.RDFXML));
  }

  @Test
  public void filter_AddAcceptHeader_WhenFormatIsTtl() throws Exception {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
    queryParameters.add("format", "ttl");
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getHeaders()).thenReturn(headers);

    // Act
    formatPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(headers).put(HttpHeaders.ACCEPT, Arrays.asList(MediaTypes.TURTLE));
  }

  @Test
  public void filter_DoNothing_WhenFormatIsUnknown() throws Exception {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();
    queryParameters.add("format", "unknown");
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

    // Act
    formatPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(headers, never()).clear();
  }

}
