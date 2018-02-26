package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import java.net.URI;
import java.util.Collections;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class BaseUriFactoryTest {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private ContainerRequest context;

  @Mock
  private URI baseUri;

  private String basePath = "/rest/v2";
  private String requestHost = "requestHost";

  @Before
  public void setUp() {
    when(baseUri.getHost()).thenReturn(requestHost);
    when(baseUri.getPort()).thenReturn(-1);
    when(context.getBaseUri()).thenReturn(baseUri);
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifXForwardedHostPresent() {
    // Arrange
    String forwardedHost = "forwardedHost";
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .basePath(basePath);
    // @formatter:on
    when(context.getRequestHeader(any())).thenReturn(Collections.singletonList(forwardedHost));

    // Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, forwardedHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifXForwardedHostNotPresent() {
    // Arrange
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .basePath(basePath);
    // @formatter:on

    // Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, requestHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_withPortNumber() {
    // Arrange
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .basePath(basePath);
    // @formatter:on
    when(baseUri.getPort()).thenReturn(123);

    //Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, requestHost + ":123", basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifSchemesEmpty() {
    // Arrange
    Swagger swagger = new Swagger().basePath(basePath);

    // Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTPS, requestHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifFirstSchemeIsHttps() {
    // Arrange
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTPS)
        .scheme(Scheme.HTTP)
        .basePath(basePath);
    // @formatter:on

    // Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTPS, requestHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifFirstSchemeIsHttp() {
    // Arrange
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .scheme(Scheme.HTTPS)
        .basePath(basePath);
    // @formatter:on

    // Act
    String baseUri = BaseUriFactory.newBaseUri(context, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, requestHost, basePath)));
  }

  @Test
  public void newBaseUri_ThrowsIllegalStateException_whenMalformed() {
    // Arrange
    when(context.getRequestHeader(any())).thenReturn(Collections.singletonList("!@#$%^&*()_+"));

    Swagger swagger = new Swagger().basePath(basePath);

    // Assert
    exception.expect(IllegalStateException.class);
    exception.expectMessage("BaseUri could not be constructed");

    // Act
    BaseUriFactory.newBaseUri(context, swagger);
  }

  private String getUriString(Scheme scheme, String host, String path) {
    return String.format("%s://%s%s", scheme.toValue(), host, path);
  }

}
