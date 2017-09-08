package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.parser.SwaggerParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerImporterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private InformationProductResourceProvider informationProductResourceProvider;

  @Mock
  private HttpConfiguration httpConfiguration;

  @Mock
  private SwaggerParser swaggerParser;

  @Mock
  private EntityBuilderAdapter entityBuilder;

  @Mock
  private org.springframework.core.io.Resource fileResource;

  @Mock
  private InformationProduct informationProduct;

  private ResourceLoader resourceLoader;

  private SwaggerImporter swaggerImporter;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    swaggerImporter =
        new SwaggerImporter(informationProductResourceProvider, swaggerParser, entityBuilder);
    swaggerImporter.setResourceLoader(resourceLoader);
  }

  @Test
  public void noDefinitionFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[0]);

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verifyZeroInteractions(informationProductResourceProvider);
    verifyZeroInteractions(httpConfiguration);
  }

  @Test
  public void nonExistingFolder() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenThrow(
        FileNotFoundException.class);

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verifyZeroInteractions(informationProductResourceProvider);
    verifyZeroInteractions(httpConfiguration);
  }

  @Test
  public void errorForDefinitionWithoutHost() throws IOException {
    // Arrange
    mockDefinition();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("OpenAPI definition document '%s' must contain a 'host' attribute.",
            DBEERPEDIA.OPENAPI_DESCRIPTION));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);
  }

  @Test
  public void ignoreUnmappedGetPaths() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("breweries",
        new Path().get(new Operation()));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verifyZeroInteractions(httpConfiguration);
  }

  @Test
  public void ignoreNonGetPaths() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).path("breweries",
        new Path().put(new Operation()));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verifyZeroInteractions(httpConfiguration);
  }

  @Test
  public void mapEndpoint() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).basePath(DBEERPEDIA.OPENAPI_BASE_PATH).produces(
        MediaType.TEXT_PLAIN).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).response(
                    200, new Response().schema(new ObjectProperty()))));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verify(httpConfiguration).registerResources(resourceCaptor.capture());

    Resource resource = resourceCaptor.getValue();
    assertThat(resourceCaptor.getAllValues(), hasSize(1));
    assertThat(resource.getPath(),
        equalTo("/" + DBEERPEDIA.OPENAPI_HOST + DBEERPEDIA.OPENAPI_BASE_PATH + "/breweries"));
    assertThat(resource.getResourceMethods(), hasSize(1));

    ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(method.getHttpMethod(), equalTo("GET"));
    assertThat(method.getProducedTypes(), hasSize(1));
    assertThat(method.getProducedTypes().get(0), equalTo(MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void mapEndpointWithoutBasePath() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(
        MediaType.TEXT_PLAIN).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).response(
                    200, new Response().schema(new ObjectProperty()))));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verify(httpConfiguration).registerResources(resourceCaptor.capture());
    Resource resource = resourceCaptor.getValue();
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
  }

  @Test
  public void mapEndpointWithoutProduces() throws IOException {
    // Arrange
    mockDefinition().host(
        DBEERPEDIA.OPENAPI_HOST).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).response(
                    200, new Response().schema(new ObjectProperty()))));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Path '%s' should produce at least one media type.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);
  }

  @Test
  public void mapEndpointWithoutOkResponse() throws IOException {
    // Arrange
    mockDefinition().host(
        DBEERPEDIA.OPENAPI_HOST).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).response(
                    201, new Response().schema(new ObjectProperty()))));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Resource '%s' does not specify a status 200 response.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);
  }

  @Test
  public void mapEndpointWithoutOkResponseSchema() throws IOException {
    // Arrange
    mockDefinition().host(
        DBEERPEDIA.OPENAPI_HOST).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).response(
                    200, new Response())));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format(
        "Resource '%s' does not specify a schema property for the status 200 response.",
        "/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);
  }

  @Test
  public void producesPrecedence() throws IOException {
    // Arrange
    mockDefinition().host(DBEERPEDIA.OPENAPI_HOST).produces(
        MediaType.TEXT_PLAIN).path(
            "/breweries",
            new Path().get(new Operation().vendorExtensions(ImmutableMap.of(
                "x-dotwebstack-information-product", DBEERPEDIA.BREWERIES.stringValue())).produces(
                    MediaType.APPLICATION_JSON).response(200,
                        new Response().schema(new ObjectProperty()))));
    when(informationProductResourceProvider.get(DBEERPEDIA.BREWERIES)).thenReturn(
        informationProduct);

    // Act
    swaggerImporter.importDefinitions(httpConfiguration);

    // Assert
    verify(httpConfiguration).registerResources(resourceCaptor.capture());
    ResourceMethod method = resourceCaptor.getValue().getResourceMethods().get(0);
    assertThat(method.getProducedTypes(), hasSize(1));
    assertThat(method.getProducedTypes().get(0), equalTo(MediaType.APPLICATION_JSON_TYPE));
  }

  private Swagger mockDefinition() throws IOException {
    when(fileResource.getInputStream()).thenReturn(IOUtils.toInputStream("spec", "UTF-8"));
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[] {fileResource});
    Swagger swagger = (new Swagger()).info(new Info().description(DBEERPEDIA.OPENAPI_DESCRIPTION));
    when(swaggerParser.parse("spec")).thenReturn(swagger);
    return swagger;
  }

}
