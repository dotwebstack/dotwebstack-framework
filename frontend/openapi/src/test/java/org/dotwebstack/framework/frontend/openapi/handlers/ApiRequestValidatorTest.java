package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableList;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.entity.ContentType;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.StreamUtils;

public class ApiRequestValidatorTest {

  private static final String BPG = "bestemmingsplangebied.1";
  private static final String ID = "id";
  private static final String EPSG = "epsg:4326";

  private Path get = new Path();
  private Path post = new Path();

  @Before
  public void before() {
    get.set("get", new Operation());
    post.set("post", new Operation());
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private ContainerRequestContext mockCtx() throws URISyntaxException {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);

    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/endpoint");
    URI requestUri = new URI("/endpoint");
    when(uriInfo.getRequestUri()).thenReturn(requestUri);

    when(ctx.getUriInfo()).thenReturn(uriInfo);
    when(ctx.getEntityStream()).thenReturn(mock(InputStream.class));

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

  private void mockBody(ContainerRequestContext ctx, String body) {
    ReaderInputStream bodyStream = new ReaderInputStream(new StringReader(body));
    when(ctx.getEntityStream()).thenReturn(bodyStream);
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
    mockBody(ctx, body);

    return ctx;
  }

  @Test
  public void validate_DoesNotFail_ForValidGetRequest() throws URISyntaxException, IOException {
    Swagger swagger = createSwagger("simple-getHeader.yml");
    RequestValidator validator = SwaggerUtils.createValidator(swagger);
    ContainerRequestContext mockGet = mockGet();
    ApiRequestValidator requestValidator1 = new ApiRequestValidator(validator);

    requestValidator1.validate(SwaggerUtils.extractApiOperation(swagger, "/endpoint", get), swagger,
        mockGet);
  }

  @Test
  public void validate_DoesNotFail_WhenRequiredHeaderIsSupplied()
      throws URISyntaxException, IOException {
    exception.expect(WebApplicationException.class);

    Swagger swagger = createSwagger("simple-getHeaderRequired.yml");
    RequestValidator validator = SwaggerUtils.createValidator(swagger);
    ContainerRequestContext mockGet = mockGet();
    ApiRequestValidator requestValidator1 = new ApiRequestValidator(validator);

    requestValidator1.validate(SwaggerUtils.extractApiOperation(swagger, "/endpoint", get), swagger,
        mockGet);
  }

  @Test
  public void validate_DoesNotFail_ForValidPostRequest() throws URISyntaxException, IOException {
    Swagger swagger = createSwagger("post-request.yml");
    RequestValidator validator = SwaggerUtils.createValidator(swagger);
    String body = "{ \"someproperty\": \"one\" }";
    ContainerRequestContext mockPost = mockPost(body);
    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, "/endpoint", post);
    ApiRequestValidator requestValidator = new ApiRequestValidator(validator);

    RequestParameters validatedParams = requestValidator.validate(apiOperation, swagger, mockPost);

    Assert.assertEquals("\"one\"", validatedParams.get("someproperty"));
  }

  @Test
  public void validate_ThrowsException_ForMissingProperty() throws URISyntaxException, IOException {
    exception.expect(WebApplicationException.class);

    Swagger swagger = createSwagger("post-request.yml");
    RequestValidator validator = SwaggerUtils.createValidator(swagger);
    ContainerRequestContext mockPost = mockPost("{ \"prop\": \"one\" }");
    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, "/endpoint", post);
    ApiRequestValidator requestValidator = new ApiRequestValidator(validator);

    requestValidator.validate(apiOperation, swagger, mockPost);
  }

  private Swagger createSwagger(String spec) throws IOException {
    String oasSpecContent = StreamUtils.copyToString(
        getClass().getResourceAsStream(getClass().getSimpleName() + "-" + spec),
        Charset.forName("UTF-8"));
    return new SwaggerParser().parse(oasSpecContent);
  }
}
