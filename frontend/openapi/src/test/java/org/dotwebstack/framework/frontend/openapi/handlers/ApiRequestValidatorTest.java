package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.http.entity.ContentType;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecUtils;
import org.dotwebstack.framework.frontend.openapi.handlers.validation.RequestValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.StreamUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApiRequestValidatorTest {

  private static final String BPG = "bestemmingsplangebied.1";
  private static final String ID = "id";
  private static final String EPSG = "epsg:4258";

  @Mock
  private RequestParameterExtractor requestParameterExtractorMock;

  private PathItem getPath = new PathItem();
  private PathItem postPath = new PathItem();
  private PathItem putPath = new PathItem();

  @Before
  public void before() {
    getPath.setGet(new Operation());
    postPath.setPost(new Operation());
    putPath.setPut(new Operation());
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ContainerRequestContext mockCtx() throws URISyntaxException {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);

    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/endpoint");

    when(ctx.getUriInfo()).thenReturn(uriInfo);

    MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();
    pathParameters.put(ID, ImmutableList.of(BPG));
    pathParameters.put("random-header-parameter", ImmutableList.of(EPSG));
    when(uriInfo.getPathParameters()).thenReturn(pathParameters);

    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.put("random-header-parameter", ImmutableList.of(EPSG));
    headers.put(HttpHeaders.CONTENT_TYPE,
        ImmutableList.of(ContentType.APPLICATION_JSON.toString()));
    when(ctx.getHeaders()).thenReturn(headers);

    return ctx;
  }

  private void mockMethod(ContainerRequestContext ctx, String method) {
    when(ctx.getMethod()).thenReturn(method);
  }

  private ContainerRequestContext mockGet() throws URISyntaxException {
    ContainerRequestContext ctx = mockCtx();
    mockMethod(ctx, HttpMethod.GET);

    return ctx;
  }

  private ContainerRequestContext mockPost(String body) throws URISyntaxException {
    ContainerRequestContext ctx = mockCtx();
    mockMethod(ctx, HttpMethod.POST);

    return ctx;
  }

  @Test
  public void validate_DoesNotFail_ForValidGetRequest() throws URISyntaxException, IOException {
    // Arrange
    OpenAPI openApi = createOpenApi("simple-getHeader.yml");

    ContainerRequestContext mockGet = mockGet();
    ApiOperation apiOperation = OpenApiSpecUtils.extractApiOperations(openApi, "/endpoint",
        getPath).iterator().next();

    when(requestParameterExtractorMock.extract(apiOperation, mockGet)).thenReturn(
        new RequestParameters());

    RequestValidator validator = OpenApiSpecUtils.createValidator(openApi);
    ApiRequestValidator requestValidator =
        new ApiRequestValidator(validator, requestParameterExtractorMock);

    // Act
    requestValidator.validate(apiOperation, mockGet);
  }

  @Test
  public void validate_Fails_WhenRequiredHeaderIsNotSupplied()
      throws URISyntaxException, IOException {
    // Assert
    exception.expect(WebApplicationException.class);

    // Arrange
    OpenAPI openApi = createOpenApi("simple-getHeaderRequired.yml");

    ApiOperation apiOperation = OpenApiSpecUtils.extractApiOperations(openApi, "/endpoint",
        getPath).iterator().next();
    ContainerRequestContext mockGet = mockGet();

    when(requestParameterExtractorMock.extract(apiOperation, mockGet)).thenReturn(
        new RequestParameters());

    RequestValidator validator = OpenApiSpecUtils.createValidator(openApi);
    ApiRequestValidator requestValidator =
        new ApiRequestValidator(validator, requestParameterExtractorMock);

    // Act
    requestValidator.validate(apiOperation, mockGet);
  }

  @Test
  public void validate_ThrowsException_WhenRequestContainsInvalidParams()
      throws URISyntaxException, IOException {
    // Assert
    exception.expect(InvalidParamsBadRequestException.class);

    // Arrange
    ContainerRequestContext mockGet = mock(ContainerRequestContext.class);

    UriInfo uriInfo = mock(UriInfo.class);

    when(mockGet.getUriInfo()).thenReturn(uriInfo);

    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
    queryParameters.put("random-query-parameter", ImmutableList.of("something?wrong"));
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

    when(uriInfo.getPathParameters()).thenReturn(new MultivaluedHashMap<>());

    OpenAPI openApi = createOpenApi("simple-getHeaderRequired.yml");

    ApiOperation apiOperation =
        OpenApiSpecUtils.extractApiOperations(openApi, "/endpoint", getPath).iterator().next();

    RequestValidator validator = OpenApiSpecUtils.createValidator(openApi);
    ApiRequestValidator requestValidator =
        new ApiRequestValidator(validator, requestParameterExtractorMock);

    // Act
    requestValidator.validate(apiOperation, mockGet);
  }

  @Test
  public void validate_DoesNotFail_ForValidPostRequest() throws URISyntaxException, IOException {
    // Arrange
    OpenAPI openApi = createOpenApi("post-request.yml");

    String body = "{ \"someproperty\": \"one\" }";
    ContainerRequestContext mockPost = mockPost(body);
    ApiOperation apiOperation = OpenApiSpecUtils.extractApiOperations(openApi, "/endpoint",
        postPath).iterator().next();

    RequestParameters requestParameters = new RequestParameters();
    when(requestParameterExtractorMock.extract(apiOperation, mockPost)).thenReturn(
        requestParameters);

    RequestValidator validator = OpenApiSpecUtils.createValidator(openApi);
    ApiRequestValidator requestValidator =
        new ApiRequestValidator(validator, requestParameterExtractorMock);

    // Act
    RequestParameters result = requestValidator.validate(apiOperation, mockPost);

    // Assert
    assertThat(result, sameInstance(requestParameters));
  }

  @Test
  public void validate_ThrowsException_ForMissingProperty() throws URISyntaxException, IOException {
    // Assert
    exception.expect(WebApplicationException.class);

    // Arrange
    OpenAPI openApi = createOpenApi("post-request.yml");

    ContainerRequestContext mockPost = mockPost("{ \"prop\": \"one\" }");
    ApiOperation apiOperation = OpenApiSpecUtils.extractApiOperations(openApi, "/endpoint",
        postPath).iterator().next();

    RequestParameters requestParameters = new RequestParameters();

    requestParameters.setRawBody("{ \"prop\": \"one\" }");
    requestParameters.put("prop", "\"one\"");

    when(requestParameterExtractorMock.extract(apiOperation, mockPost)).thenReturn(
        requestParameters);

    RequestValidator validator = OpenApiSpecUtils.createValidator(openApi);
    ApiRequestValidator requestValidator =
        new ApiRequestValidator(validator, requestParameterExtractorMock);

    // Act
    requestValidator.validate(apiOperation, mockPost);
  }

  private OpenAPI createOpenApi(String spec) throws IOException {
    String oasSpecContent = StreamUtils.copyToString(
        getClass().getResourceAsStream(getClass().getSimpleName() + "-" + spec),
        Charset.forName("UTF-8"));
    return new OpenAPIV3Parser().readContents(oasSpecContent).getOpenAPI();
  }
}
