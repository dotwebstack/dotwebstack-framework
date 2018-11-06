package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.handlers.InformationProductRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.frontend.openapi.handlers.TransactionRequestHandler;
import org.dotwebstack.framework.frontend.openapi.testutils.MockitoExtension;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class RequestMapperTest {

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  @Mock
  private TransactionRequestHandler transactionRequestHandlerMock;

  @Mock
  private InformationProductRequestHandler informationProductRequestHandlerMock;

  private OpenApiRequestMapper openApiRequestMapper;

  private ApplicationProperties applicationProperties;

  private SwaggerParseResult result = new SwaggerParseResult();

  @BeforeEach
  void setUp() {
    applicationProperties = new ApplicationProperties();
    applicationProperties
        .setOpenApiResourcePath(
            "src/test/resources/org/dotwebstack/framework/frontend/openapi/mappers/");

    RequestHandlerFactory requestHandlerFactoryMock = mock(RequestHandlerFactory.class);
    when(requestHandlerFactoryMock.newRequestHandler(any(), any(), any())) //
        .thenReturn(transactionRequestHandlerMock);

    when(requestHandlerFactoryMock.newRequestHandler(any(), any(), any(), any())) //
        .thenReturn(informationProductRequestHandlerMock);

    TransactionResourceProvider transactionResourceProviderMock =
        mock(TransactionResourceProvider.class);
    when(transactionResourceProviderMock.get(DBEERPEDIA.BREWERIES)) //
        .thenReturn(mock(Transaction.class));

    InformationProductResourceProvider informationProductResourceProviderMock =
        mock(InformationProductResourceProvider.class);
    when(informationProductResourceProviderMock.get(any())) //
        .thenReturn(mock(InformationProduct.class));

    OpenAPIV3Parser openApiParserMock = mock(OpenAPIV3Parser.class);
    when(openApiParserMock.readLocation(anyString(), anyList(), any())).thenReturn(result);
    openApiRequestMapper =
        new OpenApiRequestMapper(new OpenAPIV3Parser(), applicationProperties, Arrays.asList(
            new InformationProductRequestMapper(informationProductResourceProviderMock,
                requestHandlerFactoryMock),
            new TransactionRequestMapper(transactionResourceProviderMock,
                requestHandlerFactoryMock)));

    // openApiRequestMapper.setResourceLoader(resourceLoader);
    openApiRequestMapper.setEnvironment(mock(Environment.class));

  }

  @ParameterizedTest(name = "spec: [{0}] expected methods: [{1}]")
  @DisplayName("Map endpoints correctly with valid data")
  @CsvSource({
      "Post.yml, POST OPTIONS",
      "Put.yml, PUT OPTIONS",
      "Get.yml, GET OPTIONS",
      "Put&Post.yml, PUT POST OPTIONS",
      "Get&Put&Post.yml,GET PUT POST OPTIONS",
      "BodyParameter.yml, GET OPTIONS",
      "BodyParameterRef.yml, POST OPTIONS",
      "BodyParameterExternalRef.yml, POST OPTIONS",
  })
  void map_EndpointsCorrectly_WithValidData(String openApi, String methods)
      throws IOException {
    // Arrange
    applicationProperties
        .setOpenApiResourcePath(applicationProperties.getOpenApiResourcePath() + openApi);

    // Act
    openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    List<Resource> apiResources = verifyAndGetRegisteredResources();
    List<String> methodes = Arrays.asList(methods.split(" "));

    verifyResourcesForEndpointBreweries(apiResources, methodes.size());

    verifyMethods(apiResources, methodes);

    verifySpec(apiResources);

  }

  @ParameterizedTest(name = "{1}")
  @DisplayName("Throw exception for invalid spec")
  @CsvSource({
      "No200-TRANS.yml, does not specify a 200 response",
      "No200-IP.yml, does not specify a 200 response",
      "NoResponses.yml, does not specify a 200 response",
      "NoObjectInRequestBody.yml, No object property in body parameter.",
      "NoServer.yml, Expecting at least one server definition."
  })
  void map_ThrowsException_EndpointMissing(String openApi, String message) {
    // Arrange
    applicationProperties
        .setOpenApiResourcePath(applicationProperties.getOpenApiResourcePath() + openApi);

    // Act
    Executable action = () -> openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    assertThrows(ConfigurationException.class, action, message);
  }

  private List<Resource> verifyAndGetRegisteredResources() {
    verify(httpConfigurationMock, times(2)).registerResources(resourceCaptor.capture());

    List<Resource> apiResources = resourceCaptor.getAllValues();
    assertThat(apiResources, hasSize(2));
    return apiResources;
  }

  private void verifyResourcesForEndpointBreweries(List<Resource> apiResources, int size) {
    Resource apiResource = apiResources.get(0);
    assertThat(apiResource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
    assertThat(apiResource.getResourceMethods(), hasSize(size));
  }


  private void verifyMethods(List<Resource> apiResources, List<String> methods) {
    for (String method : methods) {
      switch (method) {
        case HttpMethod.POST:
          assertPost(apiResources);
          break;
        case HttpMethod.PUT:
          assertPut(apiResources);
          break;
        case HttpMethod.OPTIONS:
          assertOptions(apiResources);
          break;
        default:
          break;
      }
    }
  }

  private void assertOptions(List<Resource> apiResources) {
    ResourceMethod resourceMethod = getResourceMethod(apiResources, HttpMethod.OPTIONS);
    Class<?> resourceMethodHandler = resourceMethod.getInvocable().getHandler().getHandlerClass();
    assertThat(resourceMethodHandler, equalTo(OptionsRequestHandler.class));
  }

  private void assertPut(List<Resource> apiResources) {
    assertMethod(apiResources, HttpMethod.PUT);
  }

  private void assertPost(List<Resource> apiResources) {
    assertMethod(apiResources, HttpMethod.POST);
  }

  private void assertMethod(List<Resource> apiResources, String method) {
    ResourceMethod resourceMethod = getResourceMethod(apiResources, method);
    assertThat(resourceMethod.getConsumedTypes(), hasSize(1));
    assertThat(resourceMethod.getConsumedTypes(),
        contains(new javax.ws.rs.core.MediaType("application", "json")));
    assertThat(resourceMethod.getInvocable().getHandler().getInstance(null),
        sameInstance(transactionRequestHandlerMock));
  }

  private void verifySpec(List<Resource> apiResources) {
    Resource specResource = apiResources.get(1);
    assertThat(specResource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/docs/_spec"));
  }

  private ResourceMethod getResourceMethod(List<Resource> apiResources, String httpMethod) {
    Optional<ResourceMethod> optionalMethod =
        apiResources.get(0).getResourceMethods().stream().filter(
            resourceMethod -> resourceMethod.getHttpMethod().equals(httpMethod)).findFirst();
    assertThat(optionalMethod.isPresent(), is(true));
    assert optionalMethod.isPresent();
    return optionalMethod.get();
  }


}
