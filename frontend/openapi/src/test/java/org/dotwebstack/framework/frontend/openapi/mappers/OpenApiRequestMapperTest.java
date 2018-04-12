package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.InformationProductRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private InformationProductResourceProvider informationProductResourceProviderMock;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  @Mock
  private SwaggerParser openApiParserMock;

  @Mock
  private org.springframework.core.io.Resource fileResourceMock;

  @Mock
  private InformationProduct informationProductMock;

  @Mock
  private Environment environmentMock;

  @Mock
  private ApplicationProperties applicationPropertiesMock;

  @Mock
  private RequestHandlerFactory requestHandlerFactoryMock;

  private InformationProductRequestMapper informationProductRequestMapper;

  private TransactionRequestMapper transactionRequestMapper;

  @Mock
  private InformationProductRequestHandler informationProductRequestHandlerMock;

  @Mock
  private TransactionResourceProvider transactionResourceProvider;

  private ResourceLoader resourceLoader;

  private OpenApiRequestMapper requestMapper;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    when(applicationPropertiesMock.getResourcePath()).thenReturn("file:config");
    informationProductRequestMapper = new InformationProductRequestMapper(
        informationProductResourceProviderMock,requestHandlerFactoryMock);
    transactionRequestMapper = new TransactionRequestMapper(
        transactionResourceProvider, requestHandlerFactoryMock);
    requestMapper = new OpenApiRequestMapper(openApiParserMock, applicationPropertiesMock,
        informationProductRequestMapper, transactionRequestMapper);
    requestMapper.setResourceLoader(resourceLoader);
    requestMapper.setEnvironment(environmentMock);

    when(requestHandlerFactoryMock.newInformationProductRequestHandler(any(), any(),
        any(), any())).thenReturn(informationProductRequestHandlerMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_NoDefinitionFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[0]);

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verifyZeroInteractions(informationProductResourceProviderMock);
    verifyZeroInteractions(httpConfigurationMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_EmptyFile() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[] {new ByteArrayResource(new byte[0])});

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verifyZeroInteractions(informationProductResourceProviderMock);
    verifyZeroInteractions(httpConfigurationMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_NonExistingFolder() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenThrow(
        FileNotFoundException.class);

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verifyZeroInteractions(informationProductResourceProviderMock);
    verifyZeroInteractions(httpConfigurationMock);
  }

  @Test
  public void map_ThrowsException_ForDefinitionWithoutHost() throws IOException {
    // Arrange
    mockDefinition();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("OpenAPI definition document '%s' must contain a 'host' attribute.",
            DBEERPEDIA.OPENAPI_DESCRIPTION));

    // Act
    requestMapper.map(httpConfigurationMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_UnmappedGetPaths() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("breweries",
        new Path().get(new Operation()));

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verifyZeroInteractions(httpConfigurationMock);
  }

  // @Test
  // public void map_ThrowsException_NonGetPaths() throws IOException {
  // // Arrange
  // mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("breweries",
  // new Path().put(new Operation()));
  //
  // // Assert
  // thrown.expect(UnsupportedOperationException.class);
  // thrown.expectMessage("No GET operation found for");
  //
  // // Act
  // requestMapper.map(httpConfigurationMock);
  // }
  @Test
  public void map_DoesNotRegisterAnything_NonGetPaths() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("breweries",
        new Path().put(new Operation()));

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verifyZeroInteractions(httpConfigurationMock);
  }


  @Test
  public void map_EndpointsCorrectly_WithValidData() throws IOException {
    // Arrange
    Property schema = mock(Property.class);
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).basePath(DBEERPEDIA.OPENAPI_BASE_PATH).produces(
        ImmutableList.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(
                ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                    DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
                        new Response().schema(schema))));
    when(informationProductResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProductMock);

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());

    Resource resource = resourceCaptor.getValue();
    assertThat(resourceCaptor.getAllValues(), hasSize(1));
    assertThat(resource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
    assertThat(resource.getResourceMethods(), hasSize(2));

    ResourceMethod getMethod = resource.getResourceMethods().get(0);
    assertThat(getMethod.getHttpMethod(), equalTo(HttpMethod.GET));
    assertThat(getMethod.getProducedTypes(),
        contains(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE));
    assertThat(getMethod.getInvocable().getHandler().getInstance(null),
        sameInstance(informationProductRequestHandlerMock));

    ResourceMethod optionsMethod = resource.getResourceMethods().get(1);
    assertThat(optionsMethod.getHttpMethod(), equalTo(HttpMethod.OPTIONS));
    assertThat(optionsMethod.getInvocable().getHandler().getHandlerClass(),
        equalTo(OptionsRequestHandler.class));
  }

  @Test
  public void mapEndpointWithoutBasePath() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.TEXT_PLAIN).path("/breweries",
        new Path().get(new Operation().vendorExtensions(
            ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
                    new Response().schema(mock(Property.class)))));
    when(informationProductResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProductMock);

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
    Resource resource = resourceCaptor.getValue();
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
  }

  @Test
  public void map_ThrowsException_EndpointWithoutProduces() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries",
        new Path().get(new Operation().vendorExtensions(
            ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                DBEERPEDIA.BREWERIES.stringValue())).response(Status.OK.getStatusCode(),
                    new Response().schema(mock(Property.class)))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Path '%s' should produce at least one media type.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));

    // Act
    requestMapper.map(httpConfigurationMock);
  }

  @Test
  public void map_ThrowsException_EndpointWithoutResponses() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries",
        new Path().get(new Operation().vendorExtensions(
            ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                DBEERPEDIA.BREWERIES.stringValue()))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Resource '%s' does not specify a status %d response.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries", Status.OK.getStatusCode()));

    // Act
    requestMapper.map(httpConfigurationMock);
  }

  @Test
  public void map_ThrowsException_EndpointWithoutOkResponse() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("/breweries",
        new Path().get(new Operation().vendorExtensions(
            ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                DBEERPEDIA.BREWERIES.stringValue())).response(201,
                    new Response().schema(mock(Property.class)))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Resource '%s' does not specify a status %d response.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries", Status.OK.getStatusCode()));

    // Act
    requestMapper.map(httpConfigurationMock);
  }

  @Test
  public void map_BodyParameter() throws IOException {
    // Arrange
    Property property = mock(Property.class);
    List<Parameter> parameters = createBodyParameter("object");
    Operation newOp = new Operation();
    newOp.setParameters(parameters);
    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES.stringValue()));
    newOp.response(200, new Response().schema(property));
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.APPLICATION_JSON).path(
        "/breweries", new Path().get(newOp));

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
    Resource resource = resourceCaptor.getValue();
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));

  }

  private List<Parameter> createBodyRefParameter() {
    BodyParameter bodyParameter = new BodyParameter();
    RefModel schema = new RefModel();
    schema.set$ref("myref");
    bodyParameter.setSchema(schema);
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(bodyParameter);
    return parameters;
  }

  private List<Parameter> createBodyParameter(String object) {
    BodyParameter bodyParameter = new BodyParameter();
    ModelImpl schema = new ModelImpl();
    schema.setType(object);
    bodyParameter.setSchema(schema);
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(bodyParameter);
    return parameters;
  }

  @Test
  public void map_BodyParameterWithRefObject() throws IOException {
    // Arrange
    Property property = mock(Property.class);
    List<Parameter> parameters = createBodyRefParameter();
    Operation newOp = new Operation();
    newOp.setParameters(parameters);
    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES.stringValue()));
    newOp.response(200, new Response().schema(property));
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.APPLICATION_JSON).path(
        "/breweries", new Path().post(newOp));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No object property in body parameter"));

    // Act
    requestMapper.map(httpConfigurationMock);

  }

  @Test
  public void map_BodyParameterNoObject() throws IOException {
    // Arrange
    Property property = mock(Property.class);
    List<Parameter> parameters = createBodyParameter("object2");
    Operation newOp = new Operation();
    newOp.setParameters(parameters);
    newOp.vendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES.stringValue()));
    newOp.response(200, new Response().schema(property));
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.APPLICATION_JSON).path(
        "/breweries", new Path().get(newOp));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No object property in body parameter"));

    // Act
    requestMapper.map(httpConfigurationMock);

  }

  @Test
  public void map_ProducesPrecedence_WithValidData() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(MediaType.TEXT_PLAIN).path("/breweries",
        new Path().get(new Operation().vendorExtensions(
            ImmutableMap.of(OpenApiSpecificationExtensions.INFORMATION_PRODUCT,
                DBEERPEDIA.BREWERIES.stringValue())).produces(MediaType.APPLICATION_JSON).response(
                    Status.OK.getStatusCode(), new Response().schema(mock(Property.class)))));
    when(informationProductResourceProviderMock.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProductMock);

    // Act
    requestMapper.map(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
    ResourceMethod method = resourceCaptor.getValue().getResourceMethods().get(0);
    assertThat(method.getProducedTypes(), hasSize(1));
    assertThat(method.getProducedTypes().get(0), equalTo(MediaType.APPLICATION_JSON_TYPE));
  }

  private Swagger mockDefinition() throws IOException {
    byte[] bytes = "spec".getBytes(Charsets.UTF_8);
    when(fileResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[] {fileResourceMock});
    Map<String, Model> definitions = new HashMap<>();
    Model myref = new ModelImpl();

    definitions.put("myref", myref);
    Swagger swagger = (new Swagger()).info(new Info().description(DBEERPEDIA.OPENAPI_DESCRIPTION));
    swagger.setDefinitions(definitions);
    when(openApiParserMock.parse("spec")).thenReturn(swagger);

    return swagger;
  }

}
