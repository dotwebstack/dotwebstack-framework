package org.dotwebstack.framework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private BackendLoader backendLoader;

  @Test
  public void loaderMethodsCalled() throws IOException {
    // Arrange
    Application application =
        new Application(configurationBackend, backendLoader, ImmutableList.of());

    // Act
    application.load();

    // Assert
    verify(configurationBackend).initialize();
    verify(backendLoader).load();
  }

  @Test
  public void extensionPostLoadCalled() throws IOException {
    // Arrange
    Extension extensionA = mock(Extension.class);
    Extension extensionB = mock(Extension.class);
    Application application = new Application(configurationBackend, backendLoader,
        ImmutableList.of(extensionA, extensionB));

    // Act
    application.load();

    // Assert
    verify(extensionA).postLoad();
    verify(extensionB).postLoad();
  }

}
