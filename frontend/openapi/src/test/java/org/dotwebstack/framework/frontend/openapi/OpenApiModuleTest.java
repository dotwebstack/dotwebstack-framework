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
public class OpenApiModuleTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private OpenApiRequestMapper requestMapper;

  @Mock
  private HttpConfiguration httpConfiguration;

  private OpenApiModule openApiModule;

  @Before
  public void setUp() {
    openApiModule = new OpenApiModule(requestMapper);
  }

  @Test
  public void importDefinitions() throws IOException {
    // Act
    openApiModule.initialize(httpConfiguration);

    // Assert
    verify(requestMapper).map(httpConfiguration);
  }

  @Test
  public void importDefinitionsFailedIO() throws IOException {
    // Arrange
    doThrow(IOException.class).when(requestMapper).map(httpConfiguration);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Failed loading OpenAPI definitions.");

    // Act
    openApiModule.initialize(httpConfiguration);
  }

  @Test
  public void importDefinitionsFailedConfiguration() throws IOException {
    // Arrange
    doThrow(ConfigurationException.class).when(requestMapper).map(
        httpConfiguration);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    openApiModule.initialize(httpConfiguration);
  }

}
