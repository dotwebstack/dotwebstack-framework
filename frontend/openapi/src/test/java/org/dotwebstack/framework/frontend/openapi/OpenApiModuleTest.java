package org.dotwebstack.framework.frontend.openapi;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.mappers.OpenApiRequestMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiModuleTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private OpenApiRequestMapper requestMapperMock;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  private OpenApiModule openApiModule;

  @Before
  public void setUp() {
    openApiModule = new OpenApiModule(requestMapperMock);
  }

  @Test
  public void initialize_ImportDefinitions_WithConfiguration() throws IOException {
    // Act
    openApiModule.initialize(httpConfigurationMock);

    // Assert
    verify(requestMapperMock).map(httpConfigurationMock);
  }

  @Test
  public void initialize_ThrowsException_WhenImportDefinitionsFailedIo() throws IOException {
    // Arrange
    doThrow(IOException.class).when(requestMapperMock).map(httpConfigurationMock);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Failed loading OpenAPI definitions.");

    // Act
    openApiModule.initialize(httpConfigurationMock);
  }

  @Test
  public void initialize_ThrowsException_WhenImportDefinitionsFailedConfiguration()
      throws IOException {
    // Arrange
    doThrow(ConfigurationException.class).when(requestMapperMock).map(httpConfigurationMock);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    openApiModule.initialize(httpConfigurationMock);
  }

}
