package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import java.net.URI;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
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
  private ContainerRequest containerRequestMock;

  @Mock
  private URI baseUriMock;

  private String basePath = "/rest/v2";
  private String requestHost = "requestHost";

  @Before
  public void setUp() {
    when(baseUriMock.getHost()).thenReturn(requestHost);
    when(baseUriMock.getPort()).thenReturn(-1);
    when(containerRequestMock.getBaseUri()).thenReturn(baseUriMock);
    when(containerRequestMock.getRequestHeaders()).thenReturn(mock(MultivaluedStringMap.class));
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
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn(forwardedHost);

    // Act
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, forwardedHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifMultipleXForwardedHostsArePresent() {
    // Arrange
    String forwardedHost1 = "forwardedHost1";
    String forwardedHost2 = "forwardedHost2";
    // @formatter:off
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .basePath(basePath);
    // @formatter:on
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn(
        forwardedHost1 + ", " + forwardedHost2
    );

    // Act
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, forwardedHost1, basePath)));
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
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

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
    when(baseUriMock.getPort()).thenReturn(123);

    //Act
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, requestHost + ":123", basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifSchemesEmpty() {
    // Arrange
    Swagger swagger = new Swagger().basePath(basePath);

    // Act
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

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
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

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
    String baseUri = BaseUriFactory.newBaseUri(containerRequestMock, swagger);

    // Assert
    assertThat(baseUri, is(getUriString(Scheme.HTTP, requestHost, basePath)));
  }

  @Test
  public void newBaseUri_ThrowsIllegalStateException_whenMalformed() {
    // Arrange
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn("!@#$%^&*()_+");

    Swagger swagger = new Swagger().scheme(Scheme.HTTP).basePath(basePath);

    // Assert
    exception.expect(IllegalStateException.class);
    exception.expectMessage("BaseUri could not be constructed");

    // Act
    BaseUriFactory.newBaseUri(containerRequestMock, swagger);
  }

  private String getUriString(Scheme scheme, String host, String path) {
    return String.format("%s://%s%s", scheme.toValue(), host, path);
  }

}
