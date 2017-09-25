package org.dotwebstack.framework.frontend.http;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.net.HttpHeaders;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HostPreMatchingRequestFilterTest {

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private UriInfo uriInfo;

  private HostPreMatchingRequestFilter hostPreMatchingRequestFilter;

  @Before
  public void setUp() throws Exception {
    hostPreMatchingRequestFilter = new HostPreMatchingRequestFilter();

    // Arrange
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

    URI uri = URI.create("http://" + DBEERPEDIA.ORG_HOST + "/");
    when(uriInfo.getBaseUri()).thenReturn(uri);
    when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(uri));
    when(uriInfo.getPath()).thenReturn("beer");
  }

  @Test
  public void filter_PrefixPathWithHost_WhenMissingData() throws Exception {
    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri(
        "http://" + DBEERPEDIA.ORG_HOST + "/" + DBEERPEDIA.ORG_HOST + "/beer").build());
  }

  @Test
  public void filter_PrefixPathWithXForwaredHeader_Always() throws Exception {

    // Arrange
    when(containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST)).thenReturn(
        DBEERPEDIA.NL_HOST);

    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri(
        "http://" + DBEERPEDIA.ORG_HOST + "/" + DBEERPEDIA.NL_HOST + "/beer").build());
  }

  @Test
  public void filter_IgnorePort_WhenPortIsGiven() throws Exception {

    // Arrange
    when(containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST)).thenReturn(
        DBEERPEDIA.NL_HOST + ":8080");

    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Assert
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri(
        "http://" + DBEERPEDIA.ORG_HOST + "/" + DBEERPEDIA.NL_HOST + "/beer").build());
  }

}
