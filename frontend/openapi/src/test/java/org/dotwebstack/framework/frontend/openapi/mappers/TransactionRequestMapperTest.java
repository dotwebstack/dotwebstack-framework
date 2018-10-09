package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.base.Charsets;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.MockitoExtension;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.frontend.openapi.handlers.TransactionRequestHandler;
import org.dotwebstack.framework.frontend.openapi.testutils.OpenApiConverter.ToOpenApi3;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
public class TransactionRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private InformationProductResourceProvider informationProductResourceProviderMock;

  @Mock
  private TransactionResourceProvider transactionResourceProviderMock;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  @Mock
  private OpenAPIV3Parser openApiParserMock;

  @Mock
  private org.springframework.core.io.Resource fileResourceMock;

  @Mock
  private Transaction transactionMock;

  @Mock
  private Environment environmentMock;

  @Mock
  private ApplicationProperties applicationPropertiesMock;

  @Mock
  private RequestHandlerFactory requestHandlerFactoryMock;

  private List<RequestMapper> requestMappers = new ArrayList<>();

  @Mock
  private TransactionRequestHandler transactionRequestHandlerMock;

  private ResourceLoader resourceLoader;

  private OpenApiRequestMapper openApiRequestMapper;

  @BeforeEach
  void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    when(applicationPropertiesMock.getResourcePath()).thenReturn("file:config");
    requestMappers.add(new InformationProductRequestMapper(informationProductResourceProviderMock,
        requestHandlerFactoryMock));
    requestMappers.add(
        new TransactionRequestMapper(transactionResourceProviderMock, requestHandlerFactoryMock));
    openApiRequestMapper =
        new OpenApiRequestMapper(openApiParserMock, applicationPropertiesMock, requestMappers);
    openApiRequestMapper.setResourceLoader(resourceLoader);
    openApiRequestMapper.setEnvironment(environmentMock);

    when(requestHandlerFactoryMock.newTransactionRequestHandler(any(), any(), any())).thenReturn(
        transactionRequestHandlerMock);
  }

  @ParameterizedTest
  @CsvSource({"OAS3test.yml"})
  void map_PostEndpointsCorrectly_WithValidData(@ToOpenApi3 OpenAPI openAPI) throws IOException {
    // Arrange
    String specString = openAPI.toString();
    byte[] bytes = specString.getBytes(Charsets.UTF_8);
    when(fileResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString())).thenReturn(new org.springframework.core.io.Resource[] {fileResourceMock});

    when(openApiParserMock.readContents(anyString())).thenReturn(mock(SwaggerParseResult.class));
    when(openApiParserMock.readContents(anyString()).getOpenAPI()).thenReturn(openAPI);

//    Schema schema = mock(Schema.class);
//    openAPI
//        .setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SPEC_ENDPOINT, "/docs/_spec"));
//
//    Server server = new Server();
//    server.setUrl(DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH);
//    openAPI.setServers(Collections.singletonList(server));
//    Operation operation = new Operation();
//    operation.setExtensions(
//        ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION, DBEERPEDIA.BREWERIES.stringValue()));
//
//    PathItem pathItem = new PathItem();
//    pathItem.setPost(operation);
//    openAPI.path("/breweries", pathItem);
//
//    ApiResponses responses = new ApiResponses();
//    ApiResponse apiResponse = new ApiResponse();
//    Content content = new Content();
//    io.swagger.v3.oas.models.media.MediaType mediatype = new io.swagger.v3.oas.models.media.MediaType();
//    mediatype.schema(schema);
//
//    content.addMediaType("application/json", mediatype);
//    apiResponse.setContent(content);
//    responses.addApiResponse("200", apiResponse);
//    operation.setResponses(responses);
    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(transactionMock);


//    // Arrange
//    Schema schema = mock(Schema.class);
//    mockDefinition()
//        .vendorExtension(OpenApiSpecificationExtensions.SPEC_ENDPOINT, "/docs/_spec")
//        .host(DBEERPEDIA.OPENAPI_HOST)
//        .basePath(DBEERPEDIA.OPENAPI_BASE_PATH)
//        .consumes(ImmutableList.of(MediaType.APPLICATION_JSON))
//        .path("/breweries", new Path()
//            .post(new Operation()
//                .vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION, DBEERPEDIA.BREWERIES.stringValue()))
//                .response(Status.OK.getStatusCode(), new Response()
//                    .schema(schema)
//                )
//            )
//        );
//
//
//
//    .consumes(ImmutableList.of(MediaType.APPLICATION_JSON))
    // Act
    openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());

    List<Resource> apiResources = resourceCaptor.getAllValues();
    assertThat(apiResources, hasSize(2));

    Resource apiResource = apiResources.get(0);
    assertThat(apiResource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
    assertThat(apiResource.getResourceMethods(), hasSize(2));

    ResourceMethod postMethod = apiResource.getResourceMethods().get(0);
    assertThat(postMethod.getHttpMethod(), equalTo(HttpMethod.POST));
    assertThat(postMethod.getConsumedTypes(), contains(MediaType.APPLICATION_JSON_TYPE));
    assertThat(postMethod.getInvocable().getHandler().getInstance(null),
        sameInstance(transactionRequestHandlerMock));

    ResourceMethod optionsMethod = apiResource.getResourceMethods().get(1);
    assertThat(optionsMethod.getHttpMethod(), equalTo(HttpMethod.OPTIONS));
    assertThat(optionsMethod.getInvocable().getHandler().getHandlerClass(),
        equalTo(OptionsRequestHandler.class));

    Resource specResource = apiResources.get(1);
    assertThat(specResource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/docs/_spec"));
  }

//  @Test
//  public void map_PutEndpointsCorrectly_WithValidData() throws IOException {
//    // Arrange
//    Schema schema = mock(Schema.class);
//    mockDefinition().vendorExtension(OpenApiSpecificationExtensions.SPEC_ENDPOINT,
//        "/docs/_spec").host(DBEERPEDIA.OPENAPI_HOST).basePath(
//            DBEERPEDIA.OPENAPI_BASE_PATH).consumes(
//                ImmutableList.of(MediaType.APPLICATION_JSON)).path(
//                    "/breweries",
//                    new Path().put(new Operation().vendorExtensions(
//                        ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//                            DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
//                                new Response().schema(schema))));
//    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(transactionMock);
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//    // Assert
//    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());
//
//    List<Resource> apiResources = resourceCaptor.getAllValues();
//    assertThat(apiResources, hasSize(2));
//
//    Resource apiResource = apiResources.get(0);
//    assertThat(apiResource.getPath(),
//        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
//    assertThat(apiResource.getResourceMethods(), hasSize(2));
//
//    ResourceMethod putMethod = apiResource.getResourceMethods().get(0);
//    assertThat(putMethod.getHttpMethod(), equalTo(HttpMethod.PUT));
//    assertThat(putMethod.getConsumedTypes(), contains(MediaType.APPLICATION_JSON_TYPE));
//    assertThat(putMethod.getInvocable().getHandler().getInstance(null),
//        sameInstance(transactionRequestHandlerMock));
//
//    ResourceMethod optionsMethod = apiResource.getResourceMethods().get(1);
//    assertThat(optionsMethod.getHttpMethod(), equalTo(HttpMethod.OPTIONS));
//    assertThat(optionsMethod.getInvocable().getHandler().getHandlerClass(),
//        equalTo(OptionsRequestHandler.class));
//
//    Resource specResource = resourceCaptor.getAllValues().get(1);
//    assertThat(specResource.getPath(),
//        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/docs/_spec"));
//  }
//
//  @Test
//  public void map_PostAndPutEndpointsCorrectly_WithValidData() throws IOException {
//    // Arrange
//    Schema schema = mock(Schema.class);
//    mockDefinition().vendorExtension(OpenApiSpecificationExtensions.SPEC_ENDPOINT,
//        "/docs/_spec").host(DBEERPEDIA.OPENAPI_HOST).basePath(
//            DBEERPEDIA.OPENAPI_BASE_PATH).consumes(
//                ImmutableList.of(MediaType.APPLICATION_JSON)).path(
//                    "/breweries",
//                    new Path().post(new Operation().vendorExtensions(
//                        ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//                            DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
//                                new Response().schema(schema))).put(
//                                    new Operation().vendorExtensions(
//                                        ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//                                            DBEERPEDIA.BREWERIES.stringValue())).response(
//                                                Status.OK.getStatusCode(),
//                                                new Response().schema(schema))));
//    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(transactionMock);
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//    // Assert
//    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());
//
//    List<Resource> apiResources = resourceCaptor.getAllValues();
//    assertThat(apiResources, hasSize(2));
//
//    Resource apiResource = apiResources.get(0);
//    assertThat(apiResource.getPath(),
//        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
//    assertThat(apiResource.getResourceMethods(), hasSize(3));
//
//    ResourceMethod putMethod = apiResources.get(0).getResourceMethods().stream().filter(
//        resourceMethod -> resourceMethod.getHttpMethod().equals(HttpMethod.PUT)).findFirst().get();
//    assertThat(putMethod.getHttpMethod(), equalTo(HttpMethod.PUT));
//    assertThat(putMethod.getConsumedTypes(), contains(MediaType.APPLICATION_JSON_TYPE));
//    assertThat(putMethod.getInvocable().getHandler().getInstance(null),
//        sameInstance(transactionRequestHandlerMock));
//
//    ResourceMethod postMethod = apiResources.get(0).getResourceMethods().stream().filter(
//        resourceMethod -> resourceMethod.getHttpMethod().equals(HttpMethod.POST)).findFirst().get();
//    assertThat(postMethod.getConsumedTypes(), contains(MediaType.APPLICATION_JSON_TYPE));
//    assertThat(postMethod.getInvocable().getHandler().getInstance(null),
//        sameInstance(transactionRequestHandlerMock));
//
//    ResourceMethod optionsMethod = apiResources.get(0).getResourceMethods().stream().filter(
//        resourceMethod -> resourceMethod.getHttpMethod().equals(
//            HttpMethod.OPTIONS)).findFirst().get();
//    assertThat(optionsMethod.getHttpMethod(), equalTo(HttpMethod.OPTIONS));
//    assertThat(optionsMethod.getInvocable().getHandler().getHandlerClass(),
//        equalTo(OptionsRequestHandler.class));
//
//    Resource specResource = apiResources.get(1);
//    assertThat(specResource.getPath(),
//        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/docs/_spec"));
//  }
//
//  @Test
//  public void mapEndpointWithoutBasePath() throws IOException {
//    // Arrange
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).consumes(MediaType.APPLICATION_JSON).path(
//        "/breweries",
//        new Path().post(new Operation().vendorExtensions(
//            ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//                DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
//                    new Response().schema(mock(Schema.class)))));
//    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(transactionMock);
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//    // Assert
//    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());
//    Resource resource = resourceCaptor.getAllValues().get(0);
//    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
//  }
//
//  @Test
//  public void map_ThrowsException_EndpointWithoutProduces() throws IOException {
//    // Arrange
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries", new Path().put(
//        new Operation().vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//            DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
//                new Response().schema(mock(Schema.class)))));
//
//    // Assert
//    thrown.expect(ConfigurationException.class);
//    thrown.expectMessage(String.format("Path '%s' should consume at least one media type.",
//        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//  }
//
//  @Test
//  public void map_ThrowsException_EndpointWithoutResponses() throws IOException {
//    // Arrange
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries", new Path().get(
//        new Operation().vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//            DBEERPEDIA.BREWERIES.stringValue()))));
//
//    // Assert
//    thrown.expect(ConfigurationException.class);
//    thrown.expectMessage(String.format("Resource '%s' does not specify a status %d response.",
//        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries", Status.OK.getStatusCode()));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//  }
//
//  @Test
//  public void map_ThrowsException_EndpointWithoutOkResponse() throws IOException {
//    // Arrange
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries", new Path().get(
//        new Operation().vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//            DBEERPEDIA.BREWERIES.stringValue())).response(201,
//                new Response().schema(mock(Schema.class)))));
//
//    // Assert
//    thrown.expect(ConfigurationException.class);
//    thrown.expectMessage(String.format("Resource '%s' does not specify a status %d response.",
//        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries", Status.OK.getStatusCode()));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//  }
//
//  @Test
//  public void map_BodyParameter() throws IOException {
//    // Arrange
//    Schema property = mock(Schema.class);
//    List<Parameter> parameters = createBodyParameter("object");
//    Operation newOp = new Operation();
//    newOp.setParameters(parameters);
//    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//        DBEERPEDIA.BREWERIES.stringValue()));
//    newOp.response(200, new Response().schema(property));
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).consumes(MediaType.APPLICATION_JSON).path(
//        "/breweries", new Path().get(newOp));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//    // Assert
//    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());
//    Resource resource = resourceCaptor.getAllValues().get(0);
//    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
//
//  }
//
//  private List<Parameter> createBodyRefParameter() {
//    BodyParameter bodyParameter = new BodyParameter();
//    RefModel schema = new RefModel();
//    schema.set$ref("myref");
//    bodyParameter.setSchema(schema);
//    List<Parameter> parameters = new ArrayList<>();
//    parameters.add(bodyParameter);
//    return parameters;
//  }
//
//  private List<Parameter> createBodyParameter(String object) {
//    BodyParameter bodyParameter = new BodyParameter();
//    ModelImpl schema = new ModelImpl();
//    schema.setType(object);
//    bodyParameter.setSchema(schema);
//    List<Parameter> parameters = new ArrayList<>();
//    parameters.add(bodyParameter);
//    return parameters;
//  }
//
//  @Test
//  public void map_BodyParameterWithRefObject() throws IOException {
//    // Arrange
//    Schema property = mock(Schema.class);
//    List<Parameter> parameters = createBodyRefParameter();
//    Operation newOp = new Operation();
//    newOp.setParameters(parameters);
//    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//        DBEERPEDIA.BREWERIES.stringValue()));
//    newOp.response(200, new Response().schema(property));
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).consumes(MediaType.APPLICATION_JSON).path(
//        "/breweries", new Path().post(newOp));
//
//    // Assert
//    thrown.expect(ConfigurationException.class);
//    thrown.expectMessage(String.format("No object property in body parameter"));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//  }
//
//  @Test
//  public void map_BodyParameterNoObject() throws IOException {
//    // Arrange
//    Schema property = mock(Schema.class);
//    List<Parameter> parameters = createBodyParameter("object2");
//    Operation newOp = new Operation();
//    newOp.setParameters(parameters);
//    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//        DBEERPEDIA.BREWERIES.stringValue()));
//    newOp.response(200, new Response().schema(property));
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).consumes(MediaType.APPLICATION_JSON).path(
//        "/breweries", new Path().get(newOp));
//
//    // Assert
//    thrown.expect(ConfigurationException.class);
//    thrown.expectMessage(String.format("No object property in body parameter"));
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//  }
//
//  @Test
//  public void map_ProducesPrecedence_WithValidData() throws IOException {
//    // Arrange
//    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.TEXT_PLAIN).path("/breweries",
//        new Path().post(new Operation().vendorExtensions(
//            ImmutableMap.of(OpenApiSpecificationExtensions.TRANSACTION,
//                DBEERPEDIA.BREWERIES.stringValue())).consumes(MediaType.APPLICATION_JSON).response(
//                    Status.OK.getStatusCode(), new Response().schema(mock(Schema.class)))));
//    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(transactionMock);
//
//    // Act
//    openApiRequestMapper.map(httpConfigurationMock);
//
//    // Assert
//    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());
//    ResourceMethod method = resourceCaptor.getAllValues().get(0).getResourceMethods().get(0);
//    assertThat(method.getConsumedTypes(), hasSize(1));
//    assertThat(method.getConsumedTypes().get(0), equalTo(MediaType.APPLICATION_JSON_TYPE));
//  }

//  private OpenAPI mockDefinition() throws IOException {
//    String specString = "openapi: \"3.0.0\"\n" + "info:\n" + "  title: API\n" + "  version: 1.0";
//    byte[] bytes = specString.getBytes(Charsets.UTF_8);
//    when(fileResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
//    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
//        new org.springframework.core.io.Resource[] {fileResourceMock});
//
//    Map<String, Model> definitions = new HashMap<>();
//    Model myref = new ModelImpl();
//
//    definitions.put("myref", myref);
//    OpenAPI swagger = (new OpenAPI()).info(new Info().description(DBEERPEDIA.OPENAPI_DESCRIPTION));
//    swagger.setDefinitions(definitions);
//    when(openApiParserMock.read(specString)).thenReturn(swagger);

//    return swagger;
//  }

}
