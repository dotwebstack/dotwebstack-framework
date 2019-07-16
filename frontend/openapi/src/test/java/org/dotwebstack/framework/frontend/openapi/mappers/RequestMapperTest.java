package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.base.Splitter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
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
import org.junit.jupiter.api.Assertions;
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

  private static final String API_RESOURCE_PATH =
      "src/test/resources/org/dotwebstack/framework/frontend/openapi/mappers/";

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  private OpenApiRequestMapper openApiRequestMapper;

  private final ApplicationProperties applicationProperties = new ApplicationProperties();

  @BeforeEach
  void beforeEach() {
    RequestHandlerFactory requestHandlerFactoryMock = mock(RequestHandlerFactory.class);
    when(requestHandlerFactoryMock.newRequestHandler(any(), any(), any())) //
        .thenReturn(mock(TransactionRequestHandler.class));

    when(requestHandlerFactoryMock.newRequestHandler(any(), any(), any(), any())) //
        .thenReturn(mock(InformationProductRequestHandler.class));

    TransactionResourceProvider transactionRpMock = mock(TransactionResourceProvider.class);
    when(transactionRpMock.get(DBEERPEDIA.BREWERIES)).thenReturn(mock(Transaction.class));

    InformationProductResourceProvider informationProductRpMock =
        mock(InformationProductResourceProvider.class);
    when(informationProductRpMock.get(any())).thenReturn(mock(InformationProduct.class));

    openApiRequestMapper =
        new OpenApiRequestMapper(new OpenAPIV3Parser(), applicationProperties, Arrays.asList(
            new InformationProductRequestMapper(informationProductRpMock,
                requestHandlerFactoryMock),
            new TransactionRequestMapper(transactionRpMock, requestHandlerFactoryMock)),
            mock(Environment.class));
  }

  @ParameterizedTest(name = "spec: [{0}] expected methods: [{1}]")
  @DisplayName("Map endpoints correctly with valid data")
  @CsvSource({
      "Post.oas3.yml, POST OPTIONS",
      "Put.oas3.yml, PUT OPTIONS",
      "Get.oas3.yml, GET OPTIONS",
      "Delete.oas3.yml, DELETE OPTIONS",
      "Put&Post.oas3.yml, PUT POST OPTIONS",
      "Get&Put&Post.oas3.yml, GET PUT POST OPTIONS",
      "Get&Put&Post&Delete.oas3.yml, GET PUT POST DELETE OPTIONS",
      "BodyParameter.oas3.yml, GET OPTIONS",
      "BodyParameterRef.oas3.yml, POST OPTIONS",
      "BodyParameterExternalRef.oas3.yml, POST OPTIONS",
  })
  void map_EndpointsCorrectly_WithValidData(String openApi, String methodsString)
      throws IOException {
    // Arrange
    applicationProperties.setOpenApiResourcePath(API_RESOURCE_PATH + openApi);

    // Act
    openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    List<Resource> apiResources = verifyAndGetRegisteredResources();
    List<String> methods = Splitter.on(" ").splitToList(methodsString);

    verifyResourcesForEndpointBreweries(apiResources, methods.size());

    verifyMethods(apiResources, methods);

    verifySpec(apiResources);
  }

  @ParameterizedTest(name = "{1}")
  @DisplayName("Throw exception for invalid spec")
  @CsvSource({
      "No200-TRANS.oas3.yml, does not specify a 200 response",
      "No200-IP.oas3.yml, does not specify a 200 response",
      "NoResponses.oas3.yml, does not specify a 200 response",
      "NoObjectInRequestBody.oas3.yml, No object property in body parameter.",
      "NoServer.oas3.yml, Expecting at least one server definition.",
      "NonExistingFile.oas3.yml, No compatible OAS3 files found",
      "NoMediatype.oas3.yml, should produce at least one media type"
  })
  void map_ThrowsException_EndpointMissing(String openApi, String message) {
    // Arrange
    applicationProperties.setOpenApiResourcePath(API_RESOURCE_PATH + openApi);

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
    ResourceMethod optionsMethod = getResourceMethod(apiResources, HttpMethod.OPTIONS);
    Class<?> resourceMethodHandler = optionsMethod.getInvocable().getHandler().getHandlerClass();
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
    assertThat(resourceMethod.getConsumedTypes(), hasItem(new MediaType("application", "json")));
    assertThat(resourceMethod.getInvocable().getHandler().getHandlerClass(),
        equalTo(TransactionRequestHandler.class));
  }

  private void verifySpec(List<Resource> apiResources) {
    Resource specResource = apiResources.get(1);
    assertThat(specResource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/docs/_spec"));
  }

  private ResourceMethod getResourceMethod(List<Resource> apiResources, String httpMethod) {
    Optional<ResourceMethod> optionalMethod =
        apiResources.get(0).getResourceMethods().stream() //
            .filter(resourceMethod -> resourceMethod.getHttpMethod().equals(httpMethod)) //
            .findFirst();
    return optionalMethod.orElseGet(Assertions::fail);
  }


}
