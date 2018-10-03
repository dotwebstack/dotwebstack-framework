package org.dotwebstack.framework.frontend.openapi.cors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

    PathItem path = mock(PathItem.class);
    when(path.readOperationsMap()).thenReturn(
        ImmutableMap.of(PathItem.HttpMethod.GET, mock(Operation.class)));
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
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
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
        mock(PathItem.class));

    // Act
    corsResponseFilter.filter(requestContextMock, responseContextMock);

    // Assert
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo(ALLOWED_ORIGIN));
    assertThat(responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE), equalTo(86400));
    assertThat(responseHeaders.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), is(false));
    List<String> allowMethods = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(
        responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForDisallowedRequestMethodOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST);

    PathItem path = mock(PathItem.class);
    when(path.readOperationsMap()).thenReturn(
        ImmutableMap.of(PathItem.HttpMethod.GET, mock(Operation.class)));
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
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAllowedRequestMethodOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);

    PathItem path = mock(PathItem.class);
    when(path.readOperationsMap()).thenReturn(
        ImmutableMap.of(PathItem.HttpMethod.GET, mock(Operation.class)));
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
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForDisallowedRequestHeaderOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Unknown-Header");

    PathItem path = mock(PathItem.class);
    when(path.readOperationsMap()).thenReturn(
        ImmutableMap.of(PathItem.HttpMethod.GET, mock(Operation.class)));
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
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAllowedRequestHeaderOnPreflightRequest()
      throws IOException {
    // Arrange
    prepareRequest(HttpMethod.OPTIONS, ORIGIN);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET);
    requestHeaders.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "trusted-header");

    PathItem path = mock(PathItem.class);
    Operation operation = new Operation().addParametersItem(
        new HeaderParameter().name("Trusted-Header")).addParametersItem(
            new HeaderParameter().name("Other-Header"));
    when(path.readOperationsMap()).thenReturn(ImmutableMap.of(PathItem.HttpMethod.GET, operation));
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
    assertThat(allowMethods, containsInAnyOrder(PathItem.HttpMethod.GET.toString(),
        PathItem.HttpMethod.HEAD.toString(), PathItem.HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void filter_AddsCorsHeaders_ForAbsentOriginOnActualRequest() throws IOException {
    // Arrange
    prepareRequest(HttpMethod.GET, null);
    ApiResponses apiResponses = new ApiResponses();
    apiResponses.addApiResponse(HttpStatus.OK.toString(), new ApiResponse());
    Operation operation = new Operation().responses(apiResponses);
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
    Operation operation = new Operation();
    ApiResponses apiResponses = new ApiResponses();
    operation.setResponses(apiResponses);
    when(requestContextMock.getProperty(RequestHandlerProperties.OPERATION)).thenReturn(operation);
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
    ApiResponses apiResponses = new ApiResponses();
    apiResponses.addApiResponse(HttpStatus.OK.toString(), new ApiResponse());
    Operation operation = new Operation().responses(apiResponses);
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
    ApiResponses apiResponses = new ApiResponses();
    ApiResponse apiResponse = new ApiResponse().headers(
        ImmutableMap.of("Some-Header", new Header(), "Other-Header", new Header()));
    apiResponses.addApiResponse(HttpStatus.OK.toString(), apiResponse);

    Operation operation = new Operation().responses(apiResponses);
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
