package org.dotwebstack.framework.frontend.openapi;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiExtensionTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SwaggerImporter swaggerImporter;

  @Mock
  private HttpConfiguration httpConfiguration;

  private OpenApiExtension openApiExtension;

  @Before
  public void setUp() {
    openApiExtension = new OpenApiExtension(swaggerImporter);
  }

  @Test
  public void importDefinitions() throws IOException {
    // Act
    openApiExtension.initialize(httpConfiguration);

    // Assert
    verify(swaggerImporter).importDefinitions(httpConfiguration);
  }

  @Test
  public void importDefinitionsFailedIO() throws IOException {
    // Arrange
    doThrow(IOException.class).when(swaggerImporter).importDefinitions(httpConfiguration);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Failed loading OpenAPI definitions.");

    // Act
    openApiExtension.initialize(httpConfiguration);
  }

  @Test
  public void importDefinitionsFailedConfiguration() throws IOException {
    // Arrange
    doThrow(ConfigurationException.class).when(swaggerImporter).importDefinitions(
        httpConfiguration);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    openApiExtension.initialize(httpConfiguration);
  }

}
