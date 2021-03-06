package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.exception.NoContentException;
import org.dotwebstack.framework.service.openapi.exception.NotAcceptableException;
import org.dotwebstack.framework.service.openapi.exception.NotFoundException;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.requestbody.DefaultRequestBodyHandler;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseHeader;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilderTest;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoreRequestHandlerTest {

  private static final String URI_STRING = "http://dotwebstack.org/CoreRequestHandlerTest";

  @Mock
  private ResponseSchemaContext responseSchemaContext;

  @Mock
  private ResponseContextValidator responseContextValidator;

  @Mock
  private GraphQL graphQl;

  @Mock
  private JsonResponseMapper jsonResponseMapper;

  @Mock
  private ResponseMapper responseMapper;

  @Mock
  private TemplateResponseMapper templateResponseMapper;

  @Mock
  private RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  private DefaultRequestBodyHandler defaultRequestBodyHandler;

  @Mock
  private EnvironmentProperties environmentProperties;

  private final OpenAPI openApi = TestResources.openApi();

  private final ParamHandlerRouter paramHandlerRouter = new ParamHandlerRouter(Collections.emptyList(), this.openApi);

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  private CoreRequestHandler coreRequestHandler;

  @BeforeEach
  void setup() throws NotAcceptableException {
    Operation operation = this.openApi.getPaths()
        .get("/query6")
        .getGet();
    ApiResponse apiResponse = operation.getResponses()
        .get("200");
    apiResponse.getContent()
        .get("application/hal+json")
        .getSchema()
        .set$ref("#/components/schemas/Object4");

    when(this.responseSchemaContext.getParameters()).thenReturn(operation.getParameters());

    when(this.responseSchemaContext.getGraphQlField())
        .thenReturn(TestResources.getGraphQlField(TestResources.typeDefinitionRegistry(), "query6"));
    coreRequestHandler = spy(new CoreRequestHandler(openApi, "/query6", responseSchemaContext, responseContextValidator,
        graphQl, List.of(responseMapper), jsonResponseMapper, templateResponseMapper, paramHandlerRouter,
        requestBodyHandlerRouter, jexlHelper, environmentProperties));

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    doReturn(responseTemplate).when(this.coreRequestHandler)
        .getResponseTemplate(null);
  }

  @Test
  void handle_ReturnsHeaders() throws URISyntaxException {
    coreRequestHandler.handle(getServerRequest())
        .doOnSuccess(response -> {

          assertNotNull(response.headers());
          assertEquals(ImmutableList.of("value"), response.headers()
              .get("X-Response-Header"));
        });
  }

  @ParameterizedTest
  @CsvSource({"application/xml, application/xml", "application/json, application/json"})
  void getResponseTemplateTest(String acceptHeader, String expected) throws URISyntaxException {
    getServerRequest();
    List<MediaType> acceptHeaders = Collections.singletonList(MediaType.valueOf(acceptHeader));
    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeaders);

    assertThat(responseTemplate.getMediaType(), is(MediaType.valueOf(expected)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"application/xml;q=0.4,application/json", "application/*"})
  void getResponseTemplateWithQualityAndWildcardTest(String acceptHeader) throws URISyntaxException {
    getServerRequest();
    List<MediaType> acceptHeaders = Arrays.stream(acceptHeader.split(","))
        .map(MediaType::valueOf)
        .collect(Collectors.toList());

    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeaders);

    assertThat(responseTemplate.getMediaType(), is(MediaType.APPLICATION_JSON));
  }

  @Test
  void getDefaultResponseTemplateWhenNoAcceptHeaderIsProvidedTest() throws URISyntaxException {
    getServerRequest();
    List<MediaType> acceptHeaders = Collections.singletonList(MediaType.valueOf("*/*"));
    ResponseTemplate responseTemplate = coreRequestHandler.getResponseTemplate(acceptHeaders);

    assertThat(responseTemplate.getMediaType(), is(MediaType.APPLICATION_JSON));
  }

  @Test
  void getOkResponseTest() throws Exception {
    Map<Object, Object> data = new HashMap<>();
    data.put("query6", "{\"key\" : \"value\" }");

    ServerRequest request = arrangeResponseTest(data, getResponseTemplates());

    ServerResponse serverResponse = coreRequestHandler.getResponse(request, "dummyRequestId");

    assertTrue(serverResponse.statusCode()
        .is2xxSuccessful());
  }

  @Test
  void getRedirectResponseTest() throws Exception {
    Map<Object, Object> data = new HashMap<>();
    data.put("query6", "{\"key\" : \"value\" }");

    ServerRequest request = arrangeResponseTest(data, getRedirectResponseTemplate());

    ServerResponse serverResponse = coreRequestHandler.getResponse(request, "dummyRequestId");

    assertTrue(serverResponse.statusCode()
        .is3xxRedirection());
  }

  @Test
  void getParameterValidationExceptionTest() throws URISyntaxException {
    Map<Object, Object> data = new HashMap<>();
    data.put("query6", "{\"key\" : \"value\" }");

    ServerRequest request = arrangeResponseTest(data, getRedirectResponseTemplate());

    ExceptionWhileDataFetching graphQlError = mock(ExceptionWhileDataFetching.class);
    when(graphQl.execute(any(ExecutionInput.class))).thenReturn(ExecutionResultImpl.newExecutionResult()
        .addError(graphQlError)
        .build());
    when(graphQlError.getException()).thenReturn(new DirectiveValidationException("Something went wrong"));

    assertThrows(ParameterValidationException.class, () -> coreRequestHandler.getResponse(request, "dummyRequestId"));
  }

  @Test
  void shouldThrowNotFoundExceptionTest() throws URISyntaxException {
    Map<Object, Object> data = new HashMap<>();
    data.put("query6", null);

    ServerRequest request = arrangeResponseTest(data, getRedirectResponseTemplate());

    assertThrows(NotFoundException.class, () -> coreRequestHandler.getResponse(request, "dummyRequestId"));
  }

  @Test
  void shouldThrowNoContentExceptionTest() throws URISyntaxException {
    Map<Object, Object> data = new HashMap<>();
    data.put("query6", "data");

    MediaType mediaType = MediaType.valueOf("application/sparql-results+json");
    when(responseMapper.supportsInputObjectClass(any())).thenReturn(true);
    when(responseMapper.supportsOutputMimeType(mediaType)).thenReturn(true);
    when(responseMapper.toResponse(any())).thenReturn(null);

    ResponseTemplate responseTemplate = ResponseTemplate.builder()
        .mediaType(mediaType)
        .responseCode(200)
        .responseHeaders(new HashMap<>())
        .mediaType(mediaType)
        .build();

    ServerRequest request = arrangeResponseTest(data, List.of(responseTemplate));

    assertThrows(NoContentException.class, () -> coreRequestHandler.getResponse(request, "dummyRequestId"));
  }

  private ServerRequest arrangeResponseTest(Map<Object, Object> data, List<ResponseTemplate> responseTemplates)
      throws URISyntaxException {
    URI uri = new URI(URI_STRING);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    ServerRequest request = mock(ServerRequest.class);
    when(request.uri()).thenReturn(uri);
    when(request.queryParams()).thenReturn(queryParams);

    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    HttpHeaders asHeaders = mock(HttpHeaders.class);
    when(headers.asHttpHeaders()).thenReturn(asHeaders);
    when(request.headers()).thenReturn(headers);

    Mono<String> mono = Mono.empty();
    when(request.bodyToMono(String.class)).thenReturn(mono);

    ExecutionResult executionResult = mock(ExecutionResult.class);
    when(executionResult.getErrors()).thenReturn(new ArrayList<>());
    when(executionResult.getData()).thenReturn(data);

    when(graphQl.execute(any(ExecutionInput.class))).thenReturn(executionResult);
    when(responseSchemaContext.getResponses()).thenReturn(responseTemplates);
    when(jsonResponseMapper.toResponse(any(ResponseWriteContext.class))).thenReturn("{}");

    return request;
  }

  @Test
  void shouldThrowNotAcceptedExceptionTest() throws URISyntaxException {
    getServerRequest();
    List<MediaType> acceptHeader = Collections.singletonList(MediaType.valueOf("application/not_supported"));

    assertThrows(NotAcceptableException.class, () -> coreRequestHandler.getResponseTemplate(acceptHeader));
  }

  @Test
  void createResponseHeaders_returnsValue_forStaticJexlValue() {
    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    assertEquals("value", responseHeaders.get("X-Response-Header"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void createResponseHeaders_returnsValue_forJexlWithArgument() {
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .getExtensions()
        .put("x-dws-expr", "args.argument1");

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);
    Map<String, Object> inputParams = ImmutableMap.of("argument1", "argument1Value");

    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, inputParams);

    assertEquals("argument1Value", responseHeaders.get("X-Response-Header"));

  }

  @SuppressWarnings("unchecked")
  @Test
  void createResponseHeaders_returnsValue_forJexlWithEnv() {
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Header")
        .getSchema()
        .getExtensions()
        .put("x-dws-expr", "env.property1");

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    assertEquals("value1", responseHeaders.get("X-Response-Header"));
  }

  @Test
  void createResponseHeaders_returnsValue_forDefault() {
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    Map<String, String> responseHeaders = coreRequestHandler.createResponseHeaders(responseTemplate, new HashMap<>());

    assertEquals("defaultValue", responseHeaders.get("X-Response-Default"));
  }

  @Test
  void createResponseHeaders_throwsException_whenNoValueFound() {
    when(this.environmentProperties.getAllProperties()).thenReturn(ImmutableMap.of("property1", "value1"));

    this.openApi.getPaths()
        .get("/query6")
        .getGet()
        .getResponses()
        .get("200")
        .getHeaders()
        .get("X-Response-Default")
        .getSchema()
        .setDefault(null);

    ResponseTemplate responseTemplate =
        ResponseTemplateBuilderTest.getResponseTemplates(openApi, "/query6", HttpMethod.GET)
            .get(0);

    Map<String, Object> inputParams = new HashMap<>();

    assertThrows(InvalidConfigurationException.class,
        () -> coreRequestHandler.createResponseHeaders(responseTemplate, inputParams));
  }

  @Test
  void resolveUrlAndHeaderParameters_returnsValue() throws URISyntaxException {
    ServerRequest request = getServerRequest();

    Map<String, Object> params = this.coreRequestHandler.resolveUrlAndHeaderParameters(request);

    assertEquals(2, params.size());
  }

  @SuppressWarnings("rawtypes")
  @Test
  void getRequestBodyProperties_returnsEmptyProperties_forRequestBody() {
    RequestBody requestBody = this.openApi.getPaths()
        .get("/query1")
        .getPost()
        .getRequestBody();
    RequestBodyContext requestBodyContext = new RequestBodyContext(requestBody);
    ResponseSchemaContext responseSchemaContext = mock(ResponseSchemaContext.class);
    when(responseSchemaContext.getRequestBodyContext()).thenReturn(requestBodyContext);

    Map<String, Schema> requestBodyProperties =
        this.coreRequestHandler.getRequestBodyProperties(responseSchemaContext.getRequestBodyContext());
    assertFalse(requestBodyProperties.isEmpty());
    assertTrue(requestBodyProperties.containsKey("argument1"));
  }

  @Test
  void getRequestBodyProperties_returnsEmptyProperties_forNullRequestBody() {
    assertEquals(Collections.emptyMap(),
        this.coreRequestHandler.getRequestBodyProperties(responseSchemaContext.getRequestBodyContext()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void resolveParameters_returnsValues_fromRequestBody() throws BadRequestException, URISyntaxException {
    RequestBody requestBody = this.openApi.getPaths()
        .get("/query1")
        .getPost()
        .getRequestBody();
    RequestBodyContext requestBodyContext = new RequestBodyContext(requestBody);
    when(this.responseSchemaContext.getRequestBodyContext()).thenReturn(requestBodyContext);
    when(this.requestBodyHandlerRouter.getRequestBodyHandler(requestBodyContext.getRequestBodySchema()))
        .thenReturn(this.defaultRequestBodyHandler);
    when(this.defaultRequestBodyHandler.getValues(any(ServerRequest.class), any(RequestBodyContext.class),
        any(RequestBody.class), any(Map.class))).thenReturn(Map.of("key", "value"));

    assertEquals(Map.of("request_uri", URI_STRING, "query6_param1", "value1", "key", "value"),
        this.coreRequestHandler.resolveParameters(getServerRequest()));
  }

  @Test
  void resolveParameters_returnsValues_withNullRequestBodyContext() throws BadRequestException, URISyntaxException {
    ServerRequest request = getServerRequest();
    when(request.bodyToMono(String.class)).thenReturn(Mono.empty());
    when(this.responseSchemaContext.getRequestBodyContext()).thenReturn(null);

    assertEquals(Map.of("request_uri", URI_STRING, "query6_param1", "value1"),
        this.coreRequestHandler.resolveParameters(request));
  }

  ServerRequest getServerRequest() throws URISyntaxException {
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.put("query6_param1", ImmutableList.of("value1"));
    ServerRequest request = mock(ServerRequest.class);
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);

    when(responseSchemaContext.getResponses()).thenReturn(getResponseTemplates());
    when(request.uri()).thenReturn(new URI(URI_STRING));
    when(request.headers()).thenReturn(headers);
    when(request.queryParams()).thenReturn(queryParams);
    return request;
  }

  private List<ResponseTemplate> getResponseTemplates() {
    ResponseTemplate json = getTypedResponseTemplateBuilder(MediaType.APPLICATION_JSON).isDefault(true)
        .build();

    ResponseTemplate xml = getTypedResponseTemplateBuilder(MediaType.APPLICATION_XML).build();

    return List.of(json, xml);
  }

  private List<ResponseTemplate> getRedirectResponseTemplate() {
    List<ResponseTemplate> responseTemplates = new ArrayList<>();

    ResponseHeader responseHeader = ResponseHeader.builder()
        .name("Location")
        .type("string")
        .dwsExpressionMap(Map.of(X_DWS_EXPR_VALUE, "`www.kadaster.nl`"))
        .defaultValue("")
        .build();

    Map<String, ResponseHeader> responseHeaders = new HashMap<>();
    responseHeaders.put("Location", responseHeader);

    responseTemplates.add(ResponseTemplate.builder()
        .mediaType(MediaType.APPLICATION_JSON)
        .responseObject(ResponseObject.builder()
            .summary(schemaSummaryBuilder())
            .build())
        .responseCode(303)
        .responseHeaders(responseHeaders)
        .build());

    return responseTemplates;
  }

  private ResponseTemplate.ResponseTemplateBuilder getTypedResponseTemplateBuilder(MediaType mediaType) {
    return ResponseTemplate.builder()
        .responseObject(ResponseObject.builder()
            .summary(schemaSummaryBuilder())
            .build())
        .responseCode(200)
        .responseHeaders(new HashMap<>())
        .mediaType(mediaType);
  }

  private SchemaSummary schemaSummaryBuilder() {
    return SchemaSummary.builder()
        .isTransient(false)
        .required(false)
        .build();
  }

}
