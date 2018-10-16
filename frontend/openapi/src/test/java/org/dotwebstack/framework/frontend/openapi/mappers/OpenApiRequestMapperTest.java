package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.base.Charsets;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.glassfish.jersey.server.model.Resource;
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
  private OpenAPIV3Parser openApiParserMock;

  @Mock
  private org.springframework.core.io.Resource fileResourceMock;

  @Mock
  private Environment environmentMock;

  @Mock
  private ApplicationProperties applicationPropertiesMock;

  @Mock
  private RequestHandlerFactory requestHandlerFactoryMock;

  private List<RequestMapper> requestMappers = new ArrayList<>();

  @Mock
  private TransactionResourceProvider transactionResourceProvider;

  private ResourceLoader resourceLoader;

  private OpenApiRequestMapper openApiRequestMapper;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    when(applicationPropertiesMock.getResourcePath()).thenReturn("file:config");
    requestMappers.add(new InformationProductRequestMapper(informationProductResourceProviderMock,
        requestHandlerFactoryMock));
    requestMappers.add(new TransactionRequestMapper(transactionResourceProvider,
        requestHandlerFactoryMock));
    openApiRequestMapper = new OpenApiRequestMapper(openApiParserMock, applicationPropertiesMock,
        requestMappers);
    openApiRequestMapper.setResourceLoader(resourceLoader);
    openApiRequestMapper.setEnvironment(environmentMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_NoDefinitionFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[0]);

    // Act
    openApiRequestMapper.map(httpConfigurationMock);

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
    openApiRequestMapper.map(httpConfigurationMock);

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
    openApiRequestMapper.map(httpConfigurationMock);

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
    openApiRequestMapper.map(httpConfigurationMock);
  }

  @Test
  public void map_DoesNotRegisterAnything_UnmappedGetPaths() throws IOException {
    // Arrange
    mockDefinition() //
        .servers(Collections.singletonList(new Server().url("https://" + DBEERPEDIA.OPENAPI_HOST))) //
        .path("/breweries", new PathItem().get(new Operation()));

    // Act
    openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    // the spec resource is always made available
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
  }

  @Test
  public void map_DoesNotRegisterAnything_NonGetPaths() throws IOException {
    // Arrange
    mockDefinition() //
        .servers(Collections.singletonList(new Server().url("https://" + DBEERPEDIA.OPENAPI_HOST))) //
        .path("/breweries", new PathItem().get(new Operation()));

    // Act
    openApiRequestMapper.map(httpConfigurationMock);

    // Assert
    // the spec resource is always made available
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
  }

  private OpenAPI mockDefinition() throws IOException {
    String specString = ""
        + "openapi: 3.0.0\n"
        + "servers: []\n"
        + "info:\n"
        + "  title: API\n"
        + "  version: '1'\n"
        + "paths: {}";
    byte[] bytes = specString.getBytes(Charsets.UTF_8);
    when(fileResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new org.springframework.core.io.Resource[] {fileResourceMock});


    OpenAPI openApi = new OpenAPI();
    openApi.setInfo(new Info().description(DBEERPEDIA.OPENAPI_DESCRIPTION));
    openApi.getComponents().getSchemas().put("myref", new Schema<>());

    SwaggerParseResult parserResult = mock(SwaggerParseResult.class);
    when(parserResult.getOpenAPI()).thenReturn(openApi);
    when(openApiParserMock.readContents(specString)).thenReturn(parserResult);

    return openApi;
  }

}
