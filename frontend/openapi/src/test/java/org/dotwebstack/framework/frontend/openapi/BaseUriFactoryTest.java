package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.servers.Server;

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
    OpenAPI openApi = new OpenAPI() //
        .addServersItem(new Server().url("http://example.com/" + basePath));
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn(forwardedHost);

    // Act
    String baseUri =
        BaseUriFactory.determineBaseUri(containerRequestMock, openApi, new Operation());

    // Assert
    assertThat(baseUri, is(getUriString("http", forwardedHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifMultipleXForwardedHostsArePresent() {
    // Arrange
    String forwardedHost1 = "forwardedHost1";
    String forwardedHost2 = "forwardedHost2";
    // @formatter:off
    OpenAPI openApi = new OpenAPI() //
        .addServersItem(new Server().url("http://example.com/" + basePath));
    
    // @formatter:on
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn(
        forwardedHost1 + ", " + forwardedHost2);

    // Act
    String baseUri =
        BaseUriFactory.determineBaseUri(containerRequestMock, openApi, new Operation());

    // Assert
    assertThat(baseUri, is(getUriString("http", forwardedHost1, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifXForwardedHostNotPresent() {
    // Arrange
    OpenAPI openApi = new OpenAPI() //
        .addServersItem(new Server().url("http://example.com/" + basePath));

    // Act
    String baseUri =
        BaseUriFactory.determineBaseUri(containerRequestMock, openApi, new Operation());

    // Assert
    assertThat(baseUri, is(getUriString("http", requestHost, basePath)));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_withPortNumber() {
    // Arrange
    OpenAPI openApi = new OpenAPI() //
        .addServersItem(new Server().url("http://example.com/" + basePath));

    when(baseUriMock.getPort()).thenReturn(123);

    // Act
    String baseUri =
        BaseUriFactory.determineBaseUri(containerRequestMock, openApi, new Operation());

    // Assert
    assertThat(baseUri, is(getUriString("http", requestHost + ":123", basePath)));
  }


  @Test
  public void newBaseUri_ThrowsIllegalStateException_whenMalformed() {
    // Arrange
    when(containerRequestMock.getRequestHeaders().getFirst(any())).thenReturn("!@#$%^&*()_+");

    OpenAPI openApi = new OpenAPI() //
        .addServersItem(new Server().url("http://example.com/" + basePath));

    // Assert
    exception.expect(IllegalStateException.class);
    exception.expectMessage("BaseUri could not be constructed");

    // Act
    BaseUriFactory.determineBaseUri(containerRequestMock, openApi, new Operation());
  }

  // TODO: Add test cases for url in operation specification

  private String getUriString(String scheme, String host, String path) {
    return String.format("%s://%s%s", scheme, host, path);
  }

}
