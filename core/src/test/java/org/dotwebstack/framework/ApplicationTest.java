package org.dotwebstack.framework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.informationproduct.InformationProductLoader;
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

  @Mock
  private InformationProductLoader informationProductLoader;

  @Test
  public void loaderMethodsCalled() throws IOException {
    Application application = new Application(configurationBackend, backendLoader,
        informationProductLoader, ImmutableList.of());

    // Act
    application.load();

    // Assert
    verify(configurationBackend).initialize();
    verify(backendLoader).load();
    verify(informationProductLoader).load();
  }

  @Test
  public void extensionPostLoadCalled() throws IOException {
    // Arrange
    PostLoadExtension postLoadExtensionA = mock(PostLoadExtension.class);
    PostLoadExtension postLoadExtensionB = mock(PostLoadExtension.class);
    Application application = new Application(configurationBackend, backendLoader,
        informationProductLoader, ImmutableList.of(postLoadExtensionA, postLoadExtensionB));

    // Act
    application.load();

    // Assert
    verify(postLoadExtensionA).postLoad();
    verify(postLoadExtensionB).postLoad();
  }

}
