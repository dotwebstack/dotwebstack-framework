package org.dotwebstack.framework.frontend.http;

import com.google.common.net.HttpHeaders;
import com.google.common.reflect.ClassPath;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
  }

  @Test
  public void prefixPathWithHost() throws Exception {

    // Arrange
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("http://" + DBEERPEDIA.ORG_HOST));
    when(uriInfo.getPath()).thenReturn("/beer");

    when(containerRequestContext.getHeaderString(HttpHeaders.HOST)).thenReturn(DBEERPEDIA.NL_HOST);

    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Result
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri("http://" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.NL_HOST + "/beer").build());
  }

  @Test
  public void prefixPathWithXForwardedHost() throws Exception {

    // Arrange
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("http://" + DBEERPEDIA.ORG_HOST));
    when(uriInfo.getPath()).thenReturn("/beer");

    when(containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST)).thenReturn(DBEERPEDIA.NL_HOST);

    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Result
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri("http://" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.NL_HOST + "/beer").build());
  }

  @Test
  public void prefixPathWithXForwardedHostButIgnorePort() throws Exception {

    // Arrange
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("http://" + DBEERPEDIA.ORG_HOST));
    when(uriInfo.getPath()).thenReturn("/beer");

    when(containerRequestContext.getHeaderString(HttpHeaders.X_FORWARDED_HOST)).thenReturn(DBEERPEDIA.NL_HOST + ":8080");

    // Act
    hostPreMatchingRequestFilter.filter(containerRequestContext);

    // Result
    verify(containerRequestContext).setRequestUri(UriBuilder.fromUri("http://" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.NL_HOST + "/beer").build());
  }

}
