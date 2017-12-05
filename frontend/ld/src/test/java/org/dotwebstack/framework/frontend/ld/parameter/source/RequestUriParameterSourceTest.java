package org.dotwebstack.framework.frontend.ld.parameter.source;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestUriParameterSourceTest {

  RequestUriParameterSource requestUriParameterSource;

  @Mock
  ContainerRequestContext containerRequestContext;

  @Mock
  UriInfo uriInfo;

  @Before
  public void setup() {
    requestUriParameterSource = new RequestUriParameterSource();
  }

  @Test
  public void createUriParameterSource_ValidPath_WhenContextIsProvided() {
    // Arange
    URI uri = URI.create("http://" + DBEERPEDIA.ORG_HOST + "/beer");

    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getAbsolutePath()).thenReturn(uri);

    // Act/Assert
    assertThat(requestUriParameterSource.getValue(containerRequestContext),
        equalTo(uri.getScheme() + "://" + uri.getHost() + uri.getPath()));
  }
}
