package org.dotwebstack.framework.frontend.openapi.cors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.properties.StringProperty;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class CorsResponseFilterTest {

  private static final String ORIGIN = "http://foo";
  private static final String ALLOWED_ORIGIN = "*";

  @Mock
  private ContainerRequestContext requestContextMock;

  @Mock
  private ContainerResponseContext responseContextMock;

  private MultivaluedMap<String, String> requestHeaders;

  private MultivaluedMap<String, Object> responseHeaders;

  private CorsResponseFilter corsResponseFilter;

  @Before
  public void setUp() {
    corsResponseFilter = new CorsResponseFilter();

    requestHeaders = new MultivaluedHashMap<>();
    when(requestContextMock.getHeaders()).thenReturn(requestHeaders);

    responseHeaders = new MultivaluedHashMap<>();
    responseHeaders.add(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS");
    when(responseContextMock.getHeaders()).thenReturn(responseHeaders);
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentOriginOnPreflightRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, null);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);

    Path path = mock(Path.class);
    when(path.getOperationMap()).thenReturn(
        ImmutableMap.of(io.swagger.models.HttpMethod.GET, mock(Operation.class)));
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(path);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentPathPropertyOnPreflightRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.size(), is(2));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentRequestMethodOnPreflightRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(
        mock(Path.class));

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForDisallowedRequestMethodOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST);

    Path path = mock(Path.class);
    when(path.getOperationMap()).thenReturn(
        ImmutableMap.of(io.swagger.models.HttpMethod.GET, mock(Operation.class)));
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(path);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAllowedRequestMethodOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);

    Path path = mock(Path.class);
    when(path.getOperationMap()).thenReturn(
        ImmutableMap.of(io.swagger.models.HttpMethod.GET, mock(Operation.class)));
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(path);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForDisallowedRequestHeaderOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Unknown-Header");

    Path path = mock(Path.class);
    when(path.getOperationMap()).thenReturn(
        ImmutableMap.of(io.swagger.models.HttpMethod.GET, mock(Operation.class)));
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(path);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAllowedRequestHeaderOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "trusted-header");

    Path path = mock(Path.class);
    Operation operation =
        new Operation().parameter(new HeaderParameter().name("Trusted-Header")).parameter(
            new HeaderParameter().name("Other-Header"));
    when(path.getOperationMap()).thenReturn(
        ImmutableMap.of(io.swagger.models.HttpMethod.GET, operation));
    when(requestContextMock.getProperty(RequestHandlerProperties.PATH)).thenReturn(path);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    List<String> allowHeaders = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS).toString());
    assertThat(allowHeaders, containsInAnyOrder("trusted-header", "other-header"));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods,
        containsInAnyOrder(io.swagger.models.HttpMethod.GET.toString(),
            io.swagger.models.HttpMethod.HEAD.toString(),
            io.swagger.models.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentOriginOnActualRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, null);
    Operation operation = new Operation().response(HttpStatus.OK.value(), new Response());
    when(requestContextMock.getProperty(RequestHandlerProperties.OPERATION)).thenReturn(operation);
    when(responseContextMock.getStatus()).thenReturn(HttpStatus.OK.value());

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.size(), is(2));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS), is(false));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentOperationPropertyOnActualRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, ORIGIN);

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.size(), is(2));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
  }

  @Test
  public void filter_AddsCorsHeaders_ForUnknownResponseStatusOnActualRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, ORIGIN);
    when(requestContextMock.getProperty(RequestHandlerProperties.OPERATION)).thenReturn(
        mock(Operation.class));
    when(responseContextMock.getStatus()).thenReturn(HttpStatus.I_AM_A_TEAPOT.value());

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.size(), is(2));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS), is(false));
  }

  @Test
  public void filter_AddsCorsHeaders_ForPresentOriginOnActualRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, ORIGIN);
    Operation operation = new Operation().response(HttpStatus.OK.value(), new Response());
    when(requestContextMock.getProperty(RequestHandlerProperties.OPERATION)).thenReturn(operation);
    when(responseContextMock.getStatus()).thenReturn(HttpStatus.OK.value());

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS), is(false));
  }

  @Test
  public void filter_AddsCorsExposedHeaders_ForResponseWithHeadersOnActualRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, ORIGIN);
    Operation operation = new Operation().response(HttpStatus.OK.value(),
        new Response().headers(ImmutableMap.of("Some-Header", new StringProperty(), "Other-Header",
            new StringProperty())));
    when(requestContextMock.getProperty(RequestHandlerProperties.OPERATION)).thenReturn(operation);
    when(responseContextMock.getStatus()).thenReturn(HttpStatus.OK.value());

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    List<String> exposedHeaders = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS).toString());
    assertThat(exposedHeaders, containsInAnyOrder("some-header", "other-header"));
  }

  private void prepareRequest(String method, String origin) {
    when(requestContextMock.getMethod()).thenReturn(method);

    if (origin != null) {
      requestHeaders.add(HttpHeaders.ORIGIN, origin);
    }
  }

}
